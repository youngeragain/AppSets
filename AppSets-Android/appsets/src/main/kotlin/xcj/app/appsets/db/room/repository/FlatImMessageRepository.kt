package xcj.app.appsets.db.room.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.db.room.dao.FlatImMessageDao
import xcj.app.appsets.db.room.dao.GroupInfoDao
import xcj.app.appsets.db.room.dao.UserInfoDao
import xcj.app.appsets.db.room.dao.UserRelationDao
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.im.ImMessageGenerator
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserRole
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.starter.android.util.PurpleLogger

class FlatImMessageRepository private constructor(
    private val flatImMessageDao: FlatImMessageDao,
    private val userInfoDao: UserInfoDao,
    private val groupInfoDao: GroupInfoDao,
    private val userRelationDao: UserRelationDao
) {

    /**
     * 保存消息，消息的发送者信息和接收者信息单独保存避免过多重复数据
     * 如果是单聊消息，接收者信息为当前登录者
     * 如果是群组消息，接收者信息为群组，暂时不需要考虑
     */
    suspend fun saveImMessage(imMessage: ImMessage) {
        PurpleLogger.current.d(TAG, "saveImMessage")
        addFlatImMessage(imMessage)
        saveUserInfoIfNeeded(imMessage)
        saveGroupInfoIfNeeded(imMessage)
    }

    private suspend fun addFlatImMessage(imMessage: ImMessage) = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "addFlatImMessage")
        val flatImMessage = FlatImMessage.parseFromImMessage(imMessage)
        flatImMessageDao.addFlatImMessage(flatImMessage)
    }

    private suspend fun saveGroupInfoIfNeeded(imMessage: ImMessage) = withContext(Dispatchers.IO) {
        if (imMessage.toInfo.toType != ImMessage.TYPE_O2M) {
            PurpleLogger.current.d(
                TAG,
                "saveGroupInfoIfNeeded, imMessage toType is not TYPE_O2M, return"
            )
            return@withContext
        }
        if (RelationsUseCase.getInstance().hasGroupRelated(imMessage.toInfo.id)) {
            PurpleLogger.current.d(
                TAG,
                "saveGroupInfoIfNeeded, imMessage toInfo has relate, return"
            )
            return@withContext
        }
        val groupInfo = GroupInfo.basic(
            imMessage.toInfo.id,
            imMessage.toInfo.name,
            imMessage.toInfo.iconUrl
        )
        PurpleLogger.current.d(TAG, "saveGroupInfoIfNeeded, save groupInfo, $groupInfo")
        groupInfoDao.addGroup(groupInfo)
        val userRelation = UserRelation(imMessage.toInfo.id, UserRelation.TYPE_GROUP, 0)
        userRelationDao.addUserRelation(userRelation)
    }

    private suspend fun saveUserInfoIfNeeded(imMessage: ImMessage) = withContext(Dispatchers.IO) {
        if (imMessage.toInfo.toType != ImMessage.TYPE_O2O) {
            PurpleLogger.current.d(
                TAG,
                "saveUserInfoIfNeeded, imMessage toType is not TYPE_O2O, return"
            )
            return@withContext
        }

        if (imMessage.fromInfo.uid != LocalAccountManager.userInfo.uid &&
            !RelationsUseCase.getInstance().hasUserRelated(imMessage.fromInfo.uid)
        ) {
            val fromUserInfo = UserInfo.basic(
                imMessage.fromInfo.uid,
                imMessage.fromInfo.name,
                imMessage.fromInfo.avatarUrl
            )

            PurpleLogger.current.d(TAG, "saveUserInfoIfNeeded, save fromUserInfo:$fromUserInfo")
            RelationsUseCase.getInstance().addUnRelatedUid(fromUserInfo.uid)
            userInfoDao.addUserInfo(fromUserInfo)
            val userRelation = UserRelation(fromUserInfo.uid, UserRelation.TYPE_USER, 0)
            userRelationDao.addUserRelation(userRelation)
        }

        if (imMessage.toInfo.id != LocalAccountManager.userInfo.uid &&
            !RelationsUseCase.getInstance().hasUserRelated(imMessage.toInfo.id)
        ) {
            //基本不会到此
            val toUserInfo = UserInfo.basic(
                imMessage.toInfo.id,
                imMessage.toInfo.name,
                imMessage.toInfo.iconUrl
            )
            PurpleLogger.current.d(TAG, "saveUserInfoIfNeeded, save toUserInfo:$toUserInfo")
            RelationsUseCase.getInstance().addUnRelatedUid(toUserInfo.uid)
            userInfoDao.addUserInfo(toUserInfo)
            val userRelation = UserRelation(toUserInfo.uid, UserRelation.TYPE_USER, 0)
            userRelationDao.addUserRelation(userRelation)
        }
    }

    suspend fun getImMessageListByUser(
        user: UserInfo,
        page: Int = 1,
        pageSize: Int = 20
    ): List<ImMessage>? = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getImMessageListByUser, user:$user")
        val loggedUserInfo = LocalAccountManager.userInfo
        val flatImMessageList = flatImMessageDao.getFlatImMessageByUid(
            user.uid,
            loggedUserInfo.uid,
            pageSize,
            (page - 1) * pageSize
        ).reversed()
        if (flatImMessageList.isEmpty()) {
            return@withContext null
        }
        val fromInfoByUser = MessageFromInfo(user.uid, user.name, user.avatarUrl, user.roles)
        fromInfoByUser.bioUrl = user.bioUrl

        val toInfo =
            MessageToInfo(
                ImMessage.TYPE_O2O,
                loggedUserInfo.uid,
                loggedUserInfo.name,
                loggedUserInfo.avatarUrl,
                loggedUserInfo.roles
            )
        toInfo.bioUrl = loggedUserInfo.bioUrl

        val fromInfoByLoggedUser =
            MessageFromInfo(
                loggedUserInfo.uid,
                loggedUserInfo.name,
                loggedUserInfo.avatarUrl,
                loggedUserInfo.roles
            )
        fromInfoByLoggedUser.bioUrl = loggedUserInfo.bioUrl

        val imMessageList = flatImMessageList.mapNotNull { flatImMessage ->
            val fromInfo = if (flatImMessage.uid == user.uid) {
                fromInfoByUser
            } else {
                fromInfoByLoggedUser
            }
            ImMessageGenerator.generateByLocalDb(flatImMessage, fromInfo, toInfo)
        }
        return@withContext imMessageList
    }

    suspend fun getImMessageListByGroup(
        group: GroupInfo,
        page: Int = 1,
        pageSize: Int = 20
    ): List<ImMessage>? = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getImMessageListByGroup, group:$group")
        val flatImMessageList = flatImMessageDao.getFlatImMessageByGroupId(
            group.groupId,
            pageSize,
            (page - 1) * pageSize
        ).reversed()
        if (flatImMessageList.isEmpty()) {
            return@withContext null
        }
        val toInfo =
            MessageToInfo(ImMessage.TYPE_O2M, group.groupId, group.name, group.iconUrl, null)
        toInfo.bioUrl = group.bioUrl

        val uids = flatImMessageList.distinctBy { it.uid }.map { it.uid }

        val userInfoList = getUserInfoByUids(uids)
        if (userInfoList.isEmpty()) {
            return@withContext null
        }
        val userInfoMap =
            userInfoList.associateBy { it.uid }

        val imMessageList = flatImMessageList.mapNotNull { flatImMessage ->

            val messageFromUserInfo = userInfoMap[flatImMessage.uid]

            val fromInfo = MessageFromInfo(
                flatImMessage.uid,
                messageFromUserInfo?.name,
                messageFromUserInfo?.avatarUrl,
                UserRole.ROLE_UNDEFINED
            )

            fromInfo.bioUrl = messageFromUserInfo?.bioUrl

            ImMessageGenerator.generateByLocalDb(flatImMessage, fromInfo, toInfo)
        }
        return@withContext imMessageList
    }

    suspend fun getImMessageListByApplication(
        application: Application,
        page: Int = 1,
        pageSize: Int = 20
    ): List<ImMessage>? = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getImMessageListByApplication, application:$application")
        val flatImMessageList = flatImMessageDao.getFlatImMessageByGroupId(
            application.id,
            pageSize,
            (page - 1) * pageSize
        ).reversed()
        if (flatImMessageList.isEmpty()) {
            return@withContext null
        }
        val toInfo =
            MessageToInfo(
                ImMessage.TYPE_O2M,
                application.id,
                application.name,
                application.iconUrl,
                null
            )
        toInfo.bioUrl = application.bioUrl

        val uids = flatImMessageList.distinctBy { it.uid }.map { it.uid }

        val userInfoList = getUserInfoByUids(uids)
        if (userInfoList.isEmpty()) {
            return@withContext null
        }
        val userInfoMap =
            userInfoList.associateBy { it.uid }

        val imMessageList = flatImMessageList.mapNotNull { flatImMessage ->

            val messageFromUserInfo = userInfoMap[flatImMessage.uid]

            val fromInfo = MessageFromInfo(
                flatImMessage.uid,
                messageFromUserInfo?.name,
                messageFromUserInfo?.avatarUrl,
                UserRole.ROLE_UNDEFINED
            )

            fromInfo.bioUrl = messageFromUserInfo?.bioUrl

            ImMessageGenerator.generateByLocalDb(flatImMessage, fromInfo, toInfo)
        }
        return@withContext imMessageList
    }

    private suspend fun getUserInfoByUids(uids: List<String>): List<UserInfo> = withContext(
        Dispatchers.IO
    ) {
        PurpleLogger.current.d(TAG, "getUserInfoByUids, uids:$uids")
        val userInfoList = userInfoDao.getUserInfoByUids(*uids.toTypedArray())
        PictureUrlMapper.mapPictureUrl(userInfoList)
        return@withContext userInfoList
    }

    companion object {

        private const val TAG = "FlatImMessageRepository"

        private var INSTANCE: FlatImMessageRepository? = null

        fun getInstance(): FlatImMessageRepository {
            return INSTANCE ?: synchronized(this) {
                val repository = FlatImMessageRepository(
                    FlatImMessageDao.getInstance(),
                    UserInfoDao.getInstance(),
                    GroupInfoDao.getInstance(),
                    UserRelationDao.getInstance()
                )
                INSTANCE = repository
                repository
            }
        }
    }
}