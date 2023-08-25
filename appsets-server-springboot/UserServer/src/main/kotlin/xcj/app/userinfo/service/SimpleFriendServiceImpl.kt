package xcj.app.userinfo.service

import com.google.gson.Gson
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mysql.FriendDao
import xcj.app.userinfo.dao.mysql.UserDao
import xcj.app.userinfo.im.*
import xcj.app.userinfo.model.req.RequestAddFriendFeedbackParams
import xcj.app.userinfo.model.req.DeleteFriendsParams
import xcj.app.userinfo.model.req.RequestAddFriendParams
import xcj.app.userinfo.model.res.UserInfoRes
import xcj.app.userinfo.model.table.mysql.appSetsUserAdmin0
import java.time.Duration
import java.util.*

@Service
class SimpleFriendServiceImpl(
    private val tokenHelper: TokenHelper,
    private val friendDao: FriendDao,
    private val userDao: UserDao,
    private val messageBroker: MessageBroker,
    private val redisTemplate: StringRedisTemplate
):FriendService {
    override fun getAllFriendsByToken(token: String): DesignResponse<List<UserInfoRes>> {
        val uid = tokenHelper.getUidByToken(token)
        return getAllFriendsByUid(uid)
    }

    override fun getAllFriendsByUid(uid: String): DesignResponse<List<UserInfoRes>> {
        val friendUids = friendDao.getFriendUidsByUid(uid)
        return if(friendUids.isNullOrEmpty())
            DesignResponse(data = emptyList())
        else{
            val userInfoList = userDao.getUserInfoList(friendUids)
            DesignResponse(data = userInfoList)
        }

    }

    override fun deleteFriendByTokenAndFriendUid(
        token: String,
        deleteFriendParams: DeleteFriendsParams
    ): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        return deleteFriendByUidAndFriendUid(uid, deleteFriendParams)
    }

    override fun deleteFriendByUidAndFriendUid(
        uid: String,
        deleteFriendParams: DeleteFriendsParams
    ): DesignResponse<Boolean> {
        if(deleteFriendParams.friendUids.size==1){
            val friendUid = deleteFriendParams.friendUids[0]
            val deleteFriendResult = friendDao.deleteFriend(uid, friendUid)
            return if(deleteFriendResult==1)
                DesignResponse(info = "Delete the friend with friendUid:${friendUid} successful!", data = true)
            else{
                DesignResponse(info = "Delete the friend with friendUid:${friendUid} failed!", data = false)
            }
        }else if(deleteFriendParams.friendUids.size>1){
            val deleteFriendsRsult = friendDao.deleteFriends(uid, deleteFriendParams.friendUids)
            return if(deleteFriendsRsult==deleteFriendParams.friendUids.size)
                DesignResponse(info = "Delete the friend with friendUids:${deleteFriendParams.friendUids} successful!", data = true)
            else{
                DesignResponse(info = "Delete the friend with friendUids:${deleteFriendParams.friendUids} failed!", data = false)
            }
        }else{
            return DesignResponse(info = "nothing to delete!", data = false)
        }

    }


    fun sendAddFriendFeedbackImMessage(requestAddFriendFeedbackParams: RequestAddFriendFeedbackParams){
        val requestUserInfo = userDao.getUserInfoByUid(requestAddFriendFeedbackParams.requestUid)

        val friendRequestFeedbackJson = SystemContentInterface.FriendRequestFeedbackJson(
            requestAddFriendFeedbackParams.requestId,
            requestAddFriendFeedbackParams.isAccept
        )
        val gson = Gson()
        val feedbackJson = gson.toJson(friendRequestFeedbackJson)
        val systemContentJson = SystemContentJson("add_friend_request_feedback", feedbackJson)
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

    fun sendAddFriendRequestImMessage(gson: Gson,
                                      requestId:String,
                                      requestUserInfo: UserInfoRes,
                                      requestToAddUserInfo:UserInfoRes,
                                      requestAddFriendParams: RequestAddFriendParams):ImMessage {
        val appSetsUserAdmin0 = appSetsUserAdmin0()
        val messageFromInfo =
            MessageFromInfo(appSetsUserAdmin0.uid!!, appSetsUserAdmin0.name!!, appSetsUserAdmin0.avatarUrl!!, "admin")
        val messageToInfo = MessageToInfo(
            "one2one",
            requestAddFriendParams.uid,
            requestToAddUserInfo.name,
            requestToAddUserInfo.avatarUrl,
            null,
        )

        val friendRequestJson = SystemContentInterface.FriendRequestJson(
            requestId,
            requestUserInfo.uid!!,
            requestUserInfo.name, requestUserInfo.avatarUrl, requestAddFriendParams.hello
        )

        val systemContentJson = SystemContentJson("add_friend_request", gson.toJson(friendRequestJson))
        val messageContent = gson.toJson(systemContentJson)
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
        return imMessage
    }


    /**
     * 双向好友
     */
    @Transactional
    override fun requestAddFriendFeedback(token: String, requestAddFriendFeedbackParams: RequestAddFriendFeedbackParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        val requestKey =
            "add_friend_request_${requestAddFriendFeedbackParams.requestUid}_${uid}_${requestAddFriendFeedbackParams.requestId}"
        if(!redisTemplate.hasKey(requestKey))
            return DesignResponse(data = false, info = "No corresponding request was found, the response to the request to add friends is invalid!")
        sendAddFriendFeedbackImMessage(requestAddFriendFeedbackParams)

        redisTemplate.delete(requestKey)

        if(!requestAddFriendFeedbackParams.isAccept){

            return DesignResponse(data = true)
        }

        val userIdExist = userDao.isUserIdExist(requestAddFriendFeedbackParams.requestUid)
        if(!userIdExist)
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "This userId:${requestAddFriendFeedbackParams.requestUid} is not exist!", false)
        val shipExist1 = friendDao.isShipExist(uid, requestAddFriendFeedbackParams.requestUid)
        val shipExist2 = friendDao.isShipExist(requestAddFriendFeedbackParams.requestUid, uid)
        if(shipExist1&&shipExist2)
            return DesignResponse( info = "You are already friends!", data = false)
        val addFriendResult1 = friendDao.addFriend(uid, requestAddFriendFeedbackParams.requestUid)
        val addFriendResult2 = friendDao.addFriend(requestAddFriendFeedbackParams.requestUid, uid)
        return if(addFriendResult1==1&&addFriendResult2==1)
            DesignResponse(info = "Add friend successful!", data = true)
        else
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, info = "Add friend failed!", data = false)
    }

    override fun requestAddFriend(
        token: String,
        requestAddFriendParams: RequestAddFriendParams
    ): DesignResponse<String?> {
        val uid = tokenHelper.getUidByToken(token)
        val userIdExist = userDao.isUserIdExist(requestAddFriendParams.uid)
        if(!userIdExist)
            return DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "This userId:${requestAddFriendParams.uid} is not exist!", null)
        val shipExist1 = friendDao.isShipExist(uid, requestAddFriendParams.uid)
        val shipExist2 = friendDao.isShipExist(requestAddFriendParams.uid, uid)
        if(shipExist1&&shipExist2)
            return DesignResponse( info = "You are already friends!", data = null)
        val requestUserInfo = userDao.getUserInfoByUid(uid)
        val requestToAddUserInfo = userDao.getUserInfoByUid(requestAddFriendParams.uid)

        val checkKeyPrefix = "add_friend_request_${uid}_${requestToAddUserInfo.uid}_*"
        val checkKeys = redisTemplate.keys(checkKeyPrefix)
        if(checkKeys.isNotEmpty()){
            return DesignResponse(data = null, info = "You have already send a request, please wait result!")
        }
        val gson = Gson()
        val requestId = UUID.randomUUID().toString()
        val imMessage = sendAddFriendRequestImMessage(gson, requestId, requestUserInfo, requestToAddUserInfo, requestAddFriendParams)
        redisTemplate.opsForValue().set("add_friend_request_${uid}_${requestToAddUserInfo.uid}_$requestId",
            gson.toJson(imMessage), Duration.ofMinutes(10))
        return DesignResponse(data = requestId)
    }



    override fun onlineFriendUids(token: String): DesignResponse<Map<String, Boolean>> {
        val uid = tokenHelper.getUidByToken(token)
        val uids = friendDao.getFriendUidsByUid(uid)
        if(uids.isNullOrEmpty())
            return DesignResponse()
        val tokenInRedisIsExistByUids = tokenHelper.getTokenInRedisIsExistByUids(uids)
        return DesignResponse(data = tokenInRedisIsExistByUids)
    }
}










