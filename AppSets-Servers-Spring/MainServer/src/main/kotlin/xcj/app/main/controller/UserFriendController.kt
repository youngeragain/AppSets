package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.req.DeleteFriendsParams
import xcj.app.main.model.req.RequestAddFriendFeedbackParams
import xcj.app.main.model.req.RequestAddFriendParams
import xcj.app.main.model.res.UserInfoRes
import xcj.app.main.service.FriendService

@RequestMapping("/user")
@RestController
class UserFriendController(
    private val simpleFriendServiceImpl: FriendService
) {

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friends", method = [RequestMethod.GET])
    fun getAllFriends(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<List<UserInfoRes>> {
        return simpleFriendServiceImpl.getAllFriendsByToken(token)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/friends/{userId}")
    fun getAllFriendsAdmin(
        @PathVariable(name = "userId") userId: String
    ): DesignResponse<List<UserInfoRes>> {
        return simpleFriendServiceImpl.getAllFriendsByUid(userId)
    }

    /**
     * 请求添加朋友的反馈
     * @return requestId
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friend/request/feedback", method = [RequestMethod.POST])
    fun requestAddFriendFeedback(
        @RequestBody requestAddFriendFeedbackParams: RequestAddFriendFeedbackParams,
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<Boolean> {
        return simpleFriendServiceImpl.requestAddFriendFeedback(token, requestAddFriendFeedbackParams)
    }

    /**
     * 请求添加朋友
     * @return requestId
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @PostMapping("friend/request")
    fun requestAddFriend(
        @RequestBody requestAddFriendParams: RequestAddFriendParams,
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<String?> {
        return simpleFriendServiceImpl.requestAddFriend(token, requestAddFriendParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friend", method = [RequestMethod.DELETE])
    fun deleteFriend(
        @RequestBody deleteFriendsParams: DeleteFriendsParams,
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<Boolean> {
        return simpleFriendServiceImpl.deleteFriendByTokenAndFriendUid(token, deleteFriendsParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/friend/{userId}", method = [RequestMethod.DELETE])
    fun deleteFriendAdmin(
        @RequestBody deleteFriendsParams: DeleteFriendsParams,
        @PathVariable(name = "userId") userId: String,
    ): DesignResponse<Boolean> {
        return simpleFriendServiceImpl.deleteFriendByUidAndFriendUid(userId, deleteFriendsParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friends/online/uid", method = [RequestMethod.GET])
    fun onlineFriendUids(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<Map<String, Boolean>> {
        return simpleFriendServiceImpl.onlineFriendUids(token)
    }

}