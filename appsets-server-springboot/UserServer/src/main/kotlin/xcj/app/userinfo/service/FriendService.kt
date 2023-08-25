package xcj.app.userinfo.service

import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.RequestAddFriendFeedbackParams
import xcj.app.userinfo.model.req.DeleteFriendsParams
import xcj.app.userinfo.model.req.RequestAddFriendParams
import xcj.app.userinfo.model.res.UserInfoRes

interface FriendService {
    fun getAllFriendsByToken(token: String):DesignResponse<List<UserInfoRes>>
    fun getAllFriendsByUid(uid: String):DesignResponse<List<UserInfoRes>>
    fun deleteFriendByTokenAndFriendUid(token:String, deleteFriendParams: DeleteFriendsParams):DesignResponse<Boolean>
    fun deleteFriendByUidAndFriendUid(uid:String, deleteFriendParams:DeleteFriendsParams):DesignResponse<Boolean>

    fun onlineFriendUids(token: String): DesignResponse<Map<String, Boolean>>
    fun requestAddFriend(token: String, requestAddFriendParams: RequestAddFriendParams): DesignResponse<String?>
    fun requestAddFriendFeedback(token:String, requestAddFriendFeedbackParams: RequestAddFriendFeedbackParams):DesignResponse<Boolean>
}