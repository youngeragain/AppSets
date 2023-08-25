package xcj.app.userinfo.service

import xcj.app.DesignResponse

interface FollowerService{
    fun flipFollowToUserState(token: String, uid: String): DesignResponse<Boolean>
}