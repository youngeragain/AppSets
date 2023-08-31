package xcj.app.appsets.db.room.repository

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.dao.FlatImMessageDao
import xcj.app.appsets.db.room.dao.GroupInfoDao
import xcj.app.appsets.db.room.dao.UserInfoDao
import xcj.app.appsets.db.room.dao.UserRelationDao
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.im.CommonURLJson
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.RabbitMqBrokerPropertyDesignType
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.usecase.UserRelationsCase
import xcj.app.core.android.ApplicationHelper
import xcj.app.appsets.purple_module.ModuleConstant

class FlatImMessageRoomRepository {
    private val flatImMessageDao: FlatImMessageDao =
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.flatImMessageDao()
            ?: throw Exception("flatImMessageDao 未初始化")
    private val userInfoDao: UserInfoDao =
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.userInfoDao()
            ?: throw Exception("userInfoDao 未初始化")
    private val userRelationDao: UserRelationDao =
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.userRelationDao()
            ?: throw Exception("userRelationDao 未初始化")
    private val groupInfoDao: GroupInfoDao =
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.groupInfoDao()
            ?: throw Exception("groupInfoDao 未初始化")

    /**
     * 保存消息，消息的发送者信息和接收者信息单独保存避免过多重复数据
     * 如果是单聊消息，接收者信息为当前登录者
     * 如果是群组消息，接收者信息为群组，暂时不需要考虑
     */
    suspend fun addFlatImMessage(imMessage: ImMessage) {
        withContext(Dispatchers.IO) {
            val flatImMessage = imMessage.getFlatImMessage()
            flatImMessageDao.addFlatImMessage(flatImMessage)
            kotlin.runCatching {
                val toInfo = imMessage.msgToInfo
                if (toInfo.isImSingleMessage) {
                    val basicFromUserInfo = imMessage.getFromInfo()
                    if (basicFromUserInfo.uid != LocalAccountManager._userInfo.value.uid &&
                        !UserRelationsCase.getInstance().hasUserRelated(basicFromUserInfo.uid)
                    ) {
                        Log.e("FlatImMessageRoom", "addFlatImMessage 1")
                        UserRelationsCase.getInstance().addUnRelatedUid(basicFromUserInfo.uid)
                        userInfoDao.addUserInfo(basicFromUserInfo)
                        userRelationDao.addUserRelation(
                            UserRelation(
                                basicFromUserInfo.uid,
                                "user",
                                0
                            )
                        )
                    }
                    val basicToUserInfo = UserInfo.basicInfo(toInfo.id, toInfo.name, toInfo.iconUrl)
                    if (basicToUserInfo.uid != LocalAccountManager._userInfo.value.uid &&
                        !UserRelationsCase.getInstance().hasUserRelated(basicToUserInfo.uid)
                    ) {
                        Log.e("FlatImMessageRoom", "addFlatImMessage 2")
                        //基本不会到此
                        UserRelationsCase.getInstance().addUnRelatedUid(basicToUserInfo.uid)
                        userInfoDao.addUserInfo(basicToUserInfo)
                        userRelationDao.addUserRelation(
                            UserRelation(
                                basicToUserInfo.uid,
                                "user",
                                0
                            )
                        )
                    }
                } else {
                    if (!UserRelationsCase.getInstance().hasGroupRelated(toInfo.id)) {
                        val basicGroupInfo =
                            GroupInfo.basicInfo(toInfo.id, toInfo.name, toInfo.iconUrl)
                        groupInfoDao.addGroup(basicGroupInfo)
                        userRelationDao.addUserRelation(UserRelation(toInfo.id, "group", 0))
                    }
                }
            }
        }
    }

    suspend fun getImMessageListByUser(
        user: UserInfo,
        page: Int = 1,
        pageSize: Int = 20
    ): List<ImMessage> {
        val loggedUser = LocalAccountManager._userInfo.value
        val flatImMessageList = flatImMessageDao.getFlatImMessageByUid(
            user.uid,
            loggedUser.uid,
            pageSize,
            (page - 1) * pageSize
        )
        if (flatImMessageList.isEmpty())
            return emptyList()
        val messageFromInfoByUser = MessageFromInfo(user.uid, user.name, user.avatarUrl, "")
        val messageFromInfoByLoggedUser =
            MessageFromInfo(loggedUser.uid, loggedUser.name, loggedUser.avatarUrl, "")
        val messageToInfo =
            MessageToInfo(
                "one2one",
                loggedUser.uid,
                loggedUser.name,
                loggedUser.avatarUrl,
                loggedUser.roles
            )
        val gon = Gson()
        val imMessageList = flatImMessageList.mapNotNull {
            val messageFromInfo = if (it.uid == user.uid) {
                messageFromInfoByUser
            } else {
                messageFromInfoByLoggedUser
            }
            when (it.messageType) {
                RabbitMqBrokerPropertyDesignType.TYPE_TEXT -> {
                    ImMessage.Text(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_IMAGE -> {

                    ImMessage.Image(
                        it.id,
                        it.content,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_VIDEO -> {
                    val videoJson = gon.fromJson(it.content, CommonURLJson.VideoURLJson::class.java)
                    ImMessage.Video(
                        it.id,
                        videoJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_MUSIC -> {
                    val musicJson = gon.fromJson(it.content, CommonURLJson.MusicURLJson::class.java)
                    ImMessage.Music(
                        it.id,
                        musicJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_VOICE -> {
                    val voiceJson = gon.fromJson(it.content, CommonURLJson.VoiceURLJson::class.java)
                    ImMessage.Voice(
                        it.id,
                        voiceJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_LOCATION -> {
                    ImMessage.Location(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_FILE -> {
                    val fileJson = gon.fromJson(it.content, CommonURLJson.FileURLJson::class.java)
                    ImMessage.File(
                        it.id,
                        fileJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        "application/*",
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_HTML -> {
                    ImMessage.HTML(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_AD -> {
                    ImMessage.Ad(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_CUSTOM -> {
                    null
                }

                else -> null
            }
        }
        return imMessageList
    }


    fun commonMapToImMessage(flatImMessageList: List<FlatImMessage>): List<ImMessage> {
        return emptyList()
    }

    suspend fun getImMessageListByGroup(
        group: GroupInfo,
        page: Int = 1,
        pageSize: Int = 20
    ): List<ImMessage> {
        val flatImMessageList = flatImMessageDao.getFlatImMessageByGroupId(
            group.groupId,
            pageSize,
            (page - 1) * pageSize
        )
        if (flatImMessageList.isEmpty())
            return emptyList()
        val messageToInfo =
            MessageToInfo("one2many", group.groupId, group.name, group.iconUrl, null)
        val gon = Gson()
        val uids = flatImMessageList.distinctBy { it.uid }.map { it.uid }
        val userInfoMap =
            userInfoDao.getUserInfoByUids(*uids.toTypedArray())?.associateBy { it.uid }
        val imMessageList = flatImMessageList.mapNotNull {
            val messageFromInfo = MessageFromInfo(
                it.uid,
                userInfoMap?.get(it.uid)?.name ?: "",
                userInfoMap?.get(it.uid)?.avatarUrl ?: "",
                "undefined"
            )
            when (it.messageType) {
                RabbitMqBrokerPropertyDesignType.TYPE_TEXT -> {
                    ImMessage.Text(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_IMAGE -> {

                    ImMessage.Image(
                        it.id,
                        it.content,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_VIDEO -> {
                    val videoJson = gon.fromJson(it.content, CommonURLJson.VideoURLJson::class.java)
                    ImMessage.Video(
                        it.id,
                        videoJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_MUSIC -> {
                    val musicJson = gon.fromJson(it.content, CommonURLJson.MusicURLJson::class.java)
                    ImMessage.Music(
                        it.id,
                        musicJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_VOICE -> {
                    val voiceJson = gon.fromJson(it.content, CommonURLJson.VoiceURLJson::class.java)
                    ImMessage.Voice(
                        it.id,
                        voiceJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_LOCATION -> {
                    ImMessage.Location(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_FILE -> {
                    val fileJson = gon.fromJson(it.content, CommonURLJson.FileURLJson::class.java)
                    ImMessage.File(
                        it.id,
                        fileJson,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        "application/*",
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_HTML -> {
                    ImMessage.HTML(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_AD -> {
                    ImMessage.Ad(
                        it.id,
                        it.content,
                        messageFromInfo,
                        it.timestamp,
                        messageToInfo,
                        it.groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_CUSTOM -> {
                    null
                }

                else -> null
            }
        }
        return imMessageList
    }


    companion object {
        private var INSTANCE: FlatImMessageRoomRepository? = null
        fun getInstance(): FlatImMessageRoomRepository {
            return INSTANCE ?: synchronized(this) {
                val flatImMessageRoomRepository = FlatImMessageRoomRepository()
                INSTANCE = flatImMessageRoomRepository
                flatImMessageRoomRepository
            }
        }
    }
}