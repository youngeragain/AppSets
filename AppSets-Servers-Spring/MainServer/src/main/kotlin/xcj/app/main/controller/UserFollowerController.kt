package xcj.app.main.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.res.UserInfoRes
import xcj.app.main.service.FollowerService

@RequestMapping("/user")
@RestController
class UserFollowerController(
    private val simpleFollowerServiceImpl: FollowerService
) {
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("follower/state/flip/{uid}")
    fun flipFollowToUserState(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<Boolean> {
        return simpleFollowerServiceImpl.flipFollowToUserState(token, uid)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("follower/{uid}")
    fun getFollowersByUser(
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<List<UserInfoRes>?> {
        return simpleFollowerServiceImpl.getFollowersByUser(uid)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("follower/passive/{uid}")
    fun getFollowersByUserV2(
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<List<UserInfoRes>?> {
        return simpleFollowerServiceImpl.getFollowersByUser(uid)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("follower/active/{uid}")
    fun getFollowedUsersByUser(
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<List<UserInfoRes>?> {
        return simpleFollowerServiceImpl.getFollowedUsersByUser(uid)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("follower/active_passive/{uid}")
    fun getFollowersAndFollowedByUser(
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<Map<String, List<UserInfoRes>?>> {
        return simpleFollowerServiceImpl.getFollowersAndFollowedByUser(uid)
    }


    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("follower/followed/{uid}")
    fun getIsFollowedToUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<Boolean> {
        return simpleFollowerServiceImpl.getIsFollowedToUser(token, uid)
    }

}