package xcj.app.userinfo.service

import com.google.gson.Gson
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.userinfo.Helpers
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mysql.GroupDao
import xcj.app.userinfo.dao.mysql.UserDao
import xcj.app.userinfo.im.*
import xcj.app.userinfo.model.req.*
import xcj.app.userinfo.model.res.GroupInfoRes
import xcj.app.userinfo.model.res.UserInfoRes
import xcj.app.userinfo.model.table.mysql.ChatGroup
import xcj.app.userinfo.model.table.mysql.appSetsUserAdmin0
import java.time.Duration
import java.util.*

@Service
class SimpleChatGroupServiceImpl(
    private val tokenHelper: TokenHelper,
    private val groupDao: GroupDao,
    private val userDao: UserDao,
    private val messageBroker: MessageBroker,
    private val redisTemplate: StringRedisTemplate,
):ChatGroupService {
    override fun getGroupInfo(groupId: String): DesignResponse<GroupInfoRes> {
        val groupInfo = groupDao.getGroupInfoResByGroupId(groupId)
        return if(groupInfo==null){
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info = "No group found with this groupId:${groupId}!")
        }else{
            DesignResponse(data = groupInfo)
        }
    }

    override fun getUserIdsInGroup(groupId: String): DesignResponse<List<String>> {
        val userIdsInGroup = groupDao.getUserIdsInGroupByGroupId(groupId)
        return if(userIdsInGroup.isNullOrEmpty()){
            DesignResponse(ApiDesignCode.CODE_DEFAULT, "No group found with this groupId:${groupId}!")
        }else{
            DesignResponse(data = userIdsInGroup)
        }
    }

    override fun createGroup(token:String, createGroupParams: CreateGroupParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val groupNameExist = groupDao.isGroupNameExist(createGroupParams.name)
        if(groupNameExist){
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info = "This group with name:${createGroupParams.name} is existed! Please use another group name")
        }
        val groupId = Helpers.generateGroupId()
        val uids = (createGroupParams.userIds?.toMutableList()?: mutableListOf()).apply {
            add(0, uid)
        }
        val createGroupResult = groupDao.createGroup(
            createGroupParams.name,
            groupId,
            createGroupParams.type,
            createGroupParams.iconUrl,
            createGroupParams.introduction,
            if(createGroupParams.isPublic==true){1}else{0},
            if(createGroupParams.maxMembers!=null&&createGroupParams.maxMembers>0){createGroupParams.maxMembers}else{100},
            uid,
            uids)
        return if(createGroupResult==1)
            DesignResponse(info = "Crate group with name:${createGroupParams.name} successful!", data = true)
        else
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info = "Crate group with name:${createGroupParams.name} failed!", data = false)
    }

    override fun createGroupPreCheck(groupName: String): DesignResponse<Boolean> {
        val groupNameExist = groupDao.isGroupNameExist(groupName)
        return DesignResponse(data = !groupNameExist)
    }


    override fun getGroupInfoListByUserId(userId: String): DesignResponse<List<GroupInfoRes>> {
        val groupInfoList = groupDao.getGroupInfoResListByUserId(userId)
        return if(groupInfoList.isNullOrEmpty()){
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info = "No groups found with this userId:${userId}!")
        }else{
            DesignResponse(data = groupInfoList)
        }
    }

    override fun getGroupInfoListByToken(token: String): DesignResponse<List<GroupInfoRes>> {
        val uid = tokenHelper.getUidByToken(token)
        return getGroupInfoListByUserId(uid)
    }

    override fun deleteGroup(deleteGroupParams: DeleteGroupParams): DesignResponse<Boolean> {
        //sendNotification to user with deleteReason
        val deleteGroupResult = groupDao.deleteGroup(deleteGroupParams.groupId)
        return if(deleteGroupResult==1)
            DesignResponse(info = "Delete this group successful!, groupId:${deleteGroupParams.groupId}")
        else
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Delete this group failed!, groupId:${deleteGroupParams.groupId}")
    }

    override fun deleteUsersInGroup(deleteUsersInGroupParams: DeleteUsersInGroupParams): DesignResponse<Boolean> {
        //sendNotification to user with deleteReason
        val groupIdExist = groupDao.isGroupIdExist(deleteUsersInGroupParams.groupId)
        if(!groupIdExist){
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Delete users in group:${deleteUsersInGroupParams.groupId} failed!, group:${deleteUsersInGroupParams.groupId} is not exist!", false)
        }
        val deleteUsersInGroupResult =
            groupDao.deleteUsersInGroup(deleteUsersInGroupParams.groupId, deleteUsersInGroupParams.userIds)
        return if(deleteUsersInGroupResult==deleteUsersInGroupParams.userIds.size)
            DesignResponse(info = "Delete users in this group successful!, userIds:${deleteUsersInGroupParams.userIds} in groupId:${deleteUsersInGroupParams.groupId}")
        else
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Delete users in this group failed!, userIds:${deleteUsersInGroupParams.userIds} in groupId:${deleteUsersInGroupParams.groupId}:")
    }

    override fun requestJoinGroupFeedback(requestJoinGroupFeedbackParams: RequestJoinGroupFeedbackParams): DesignResponse<Boolean> {
        val requestKey =
            "add_group_request_${requestJoinGroupFeedbackParams.requestUid}_${requestJoinGroupFeedbackParams.groupId}_${requestJoinGroupFeedbackParams.requestId}"
        if(!redisTemplate.hasKey(requestKey)){
            return DesignResponse(data = false, info = "No corresponding request was found, the response for joining the group is invalid!")
        }
        sendJoinGroupFeedbackImMessage(requestJoinGroupFeedbackParams)
        redisTemplate.delete(requestKey)
        if(!requestJoinGroupFeedbackParams.isAccept){
            return DesignResponse(data = true)
        }
        val groupIdExist = groupDao.isGroupIdExist(requestJoinGroupFeedbackParams.groupId)
        if(!groupIdExist)
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "This group:${requestJoinGroupFeedbackParams.groupId} is not exist!", false)
        val findUserCountByUserIdsResult = userDao.findUserCountByUserIds(requestJoinGroupFeedbackParams.userIds)
        if(findUserCountByUserIdsResult!=requestJoinGroupFeedbackParams.userIds.size)
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "There are some userIds that don't exist, Please check!")
        val addUsersInGroupResult = groupDao.addUsersInGroup(requestJoinGroupFeedbackParams.groupId, requestJoinGroupFeedbackParams.userIds)
        return if(addUsersInGroupResult==requestJoinGroupFeedbackParams.userIds.size){
            DesignResponse(info = "Add users${requestJoinGroupFeedbackParams.userIds} to group:${requestJoinGroupFeedbackParams.groupId} successful!", data = true)
        }else{
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info = "Add users${requestJoinGroupFeedbackParams.userIds} to group:${requestJoinGroupFeedbackParams.groupId} failed!", data = false)
        }
    }

    private fun sendJoinGroupFeedbackImMessage(requestJoinGroupFeedbackParams: RequestJoinGroupFeedbackParams) {
        val requestUserInfo = userDao.getUserInfoByUid(requestJoinGroupFeedbackParams.requestUid)

        val friendRequestFeedbackJson = SystemContentInterface.FriendRequestFeedbackJson(
            requestJoinGroupFeedbackParams.requestId,
            requestJoinGroupFeedbackParams.isAccept
        )
        val gson = Gson()
        val feedbackJson = gson.toJson(friendRequestFeedbackJson)
        val systemContentJson = SystemContentJson("join_group_request_feedback", feedbackJson)
        val messageContent = gson.toJson(systemContentJson)

        val appSetsUserAdmin0 = appSetsUserAdmin0()
        val messageFromInfo =
            MessageFromInfo(appSetsUserAdmin0.uid!!, appSetsUserAdmin0.name!!, appSetsUserAdmin0.avatarUrl!!, "admin")

        val messageToInfo = MessageToInfo("one2one", requestUserInfo.uid, requestUserInfo.name, requestUserInfo.avatarUrl, null)

        val imMessage = ImMessage(
            UUID.randomUUID().toString(),
            messageContent,
            null,
            messageFromInfo,
            Date(),
            messageToInfo,
            null
        )

        messageBroker.sendMessage(imMessage)
    }

    override fun searchChatGroupsByKeywords(
        keywords: String,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<GroupInfoRes>> {
        val limit = pageSize?:20
        val offset = ((page?:1)-1)*limit
        val groupInfoResList = groupDao.searchChatGroupResListByKeywords(keywords, limit, offset)
        return DesignResponse(data = groupInfoResList)
    }

    override fun requestJoinGroup(token: String, requestJoinGroupParams: RequestJoinGroupParams): DesignResponse<String?> {
        val uid = tokenHelper.getUidByToken(token)
        val existInGroup = groupDao.isUserExistInGroup(requestJoinGroupParams.groupId, uid)
        if(existInGroup)
            return DesignResponse(data = null, info = "You are already in group!")
        val requestToJoinGroupInfo = groupDao.getChatGroupInfoByGroupId(requestJoinGroupParams.groupId)
        if(requestToJoinGroupInfo==null){
            return DesignResponse(data = null, info = "Request group doest not exist!")
        }
        val requestGroupOwnerUserInfo = userDao.getUserInfoByUid(requestToJoinGroupInfo.currentOwnerUid)
        val requestUserInfo = userDao.getUserInfoByUid(uid)
        val checkKeyPrefix = "add_group_request_${uid}_${requestJoinGroupParams.groupId}_*"
        val checkKeys = redisTemplate.keys(checkKeyPrefix)
        if(checkKeys.isNotEmpty()){
            return DesignResponse(data = null, info = "You have already send a request, please wait result!")
        }

        val requestId = UUID.randomUUID().toString()
        val gson = Gson()
        val imMessage:ImMessage = sendJoinGroupRequestImMessage(gson, uid, requestId, requestUserInfo,
            requestGroupOwnerUserInfo, requestToJoinGroupInfo, requestJoinGroupParams)
        redisTemplate.opsForValue().set("add_group_request_${uid}_${requestJoinGroupParams.groupId}_$requestId",
            gson.toJson(imMessage), Duration.ofMinutes(10))
        return DesignResponse(data = requestId)
    }

    private fun sendJoinGroupRequestImMessage(
        gson: Gson,
        uid: String,
        requestId: String,
        requestUserInfo: UserInfoRes,
        requestGroupOwnerUserInfo:UserInfoRes,
        requestToJoinGroupInfo: ChatGroup,
        requestJoinGroupParams: RequestJoinGroupParams
    ): ImMessage {
        val groupRequestJson = SystemContentInterface.GroupRequestJson(
            requestId,
            uid,
            requestUserInfo.name,
            requestUserInfo.avatarUrl,
            requestJoinGroupParams.hello,
            requestJoinGroupParams.groupId,
            requestToJoinGroupInfo.name,
            requestToJoinGroupInfo.iconUrl)

        val systemContentJson = SystemContentJson("join_group_request", gson.toJson(groupRequestJson))
        val messageContent = gson.toJson(systemContentJson)
        val appSetsUserAdmin0 = appSetsUserAdmin0()
        val messageFromInfo =
            MessageFromInfo(appSetsUserAdmin0.uid!!, appSetsUserAdmin0.name!!, appSetsUserAdmin0.avatarUrl!!, "admin")
        val messageToInfo =
            MessageToInfo("one2one", requestGroupOwnerUserInfo.uid, requestGroupOwnerUserInfo.name, requestGroupOwnerUserInfo.avatarUrl, null)
        val imMessage = ImMessage(
            UUID.randomUUID().toString(),
            messageContent,
            null,
            messageFromInfo,
            Date(),
            messageToInfo,
            null)

        messageBroker.sendMessage(imMessage)
        return imMessage
    }
}