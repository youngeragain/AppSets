package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.userinfo.service.FollowerService

@RequestMapping("/user")
@RestController
class UserFollowerController(
    private val simpleFollowerServiceImpl: FollowerService
) {
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("follower/state/flip", method = [RequestMethod.GET])
    fun flipFollowToUserState(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) uid:String
    ):DesignResponse<Boolean>{
        return simpleFollowerServiceImpl.flipFollowToUserState(token, uid)
    }

}