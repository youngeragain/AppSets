package xcj.app.userinfo.service

import xcj.app.DesignResponse
import xcj.app.userinfo.model.res.UserInfoRes

interface FollowerService{
    fun flipFollowToUserState(token: String, uid: String): DesignResponse<Boolean>
    fun getFollowersByUser(uid: String): DesignResponse<List<UserInfoRes>?>
    fun getIsFollowedToUser(token: String, uid: String): DesignResponse<Boolean>
    fun getFollowedUsersByUser(uid: String): DesignResponse<List<UserInfoRes>?>
    fun getFollowersAndFollowedByUser(uid: String): DesignResponse<Map<String, List<UserInfoRes>?>>
}