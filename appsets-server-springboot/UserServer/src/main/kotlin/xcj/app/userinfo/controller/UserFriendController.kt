package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.ApiDesignEncodeStr
import xcj.app.userinfo.model.req.RequestAddFriendFeedbackParams
import xcj.app.userinfo.model.req.DeleteFriendsParams
import xcj.app.userinfo.model.req.RequestAddFriendParams
import xcj.app.userinfo.model.res.UserInfoRes
import xcj.app.userinfo.service.FriendService

@RequestMapping("/user")
@RestController
class UserFriendController(
    private val simpleFriendServiceImpl: FriendService
) {

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friends", method = [RequestMethod.GET])
    fun getAllFriends(@RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String):DesignResponse<List<UserInfoRes>>{
        return simpleFriendServiceImpl.getAllFriendsByToken(token)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/friends/{userId}")
    fun getAllFriendsAdmin(@PathVariable(name = "userId") userId: String):DesignResponse<List<UserInfoRes>>{
        return simpleFriendServiceImpl.getAllFriendsByUid(userId)
    }


    /**
     * 请求添加朋友的反馈
     * @return requestId
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friend/request/feedback", method = [RequestMethod.POST])
    fun requestAddFriendFeedback(@RequestBody requestAddFriendFeedbackParams: RequestAddFriendFeedbackParams,
                  @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String):DesignResponse<Boolean>{
        return simpleFriendServiceImpl.requestAddFriendFeedback(token, requestAddFriendFeedbackParams)
    }


    /**
     * 请求添加朋友
     * @return requestId
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @PostMapping("friend/request")
    fun requestAddFriend(@RequestBody requestAddFriendParams: RequestAddFriendParams,
                  @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String):DesignResponse<String?>{
        return simpleFriendServiceImpl.requestAddFriend(token, requestAddFriendParams)
    }


    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friend", method = [RequestMethod.DELETE])
    fun deleteFriend(@RequestBody deleteFriendsParams: DeleteFriendsParams,
                  @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String):DesignResponse<Boolean>{
        return simpleFriendServiceImpl.deleteFriendByTokenAndFriendUid(token, deleteFriendsParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/friend/{userId}", method = [RequestMethod.DELETE])
    fun deleteFriendAdmin(@RequestBody deleteFriendsParams: DeleteFriendsParams,
                          @PathVariable(name = "userId") userId: String,
                     ):DesignResponse<Boolean>{
        return simpleFriendServiceImpl.deleteFriendByUidAndFriendUid(userId, deleteFriendsParams)
    }


    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("friends/online/uid", method = [RequestMethod.GET])
    fun onlineFriendUids(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String
    ):DesignResponse<Map<String, Boolean>>{
        return simpleFriendServiceImpl.onlineFriendUids(token)
    }

}