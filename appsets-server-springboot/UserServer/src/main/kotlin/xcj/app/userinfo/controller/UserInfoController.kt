package xcj.app.userinfo.controller

import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.ApiDesignEncodeStr
import xcj.app.userinfo.model.req.UpdateUserInfoParams
import xcj.app.userinfo.model.res.UserInfoRes
import xcj.app.userinfo.service.UserInfoService
import org.springframework.web.bind.annotation.*


@RequestMapping("/user")
@RestController
class UserInfoController(
    private val simpleUserInfoServiceImpl: UserInfoService) {

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("info/get/{uid}")
    fun getInfo(
        @PathVariable(required = false) uid:String,
    ):DesignResponse<UserInfoRes>{
        return simpleUserInfoServiceImpl.getUserInfoByUid(uid)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("info/get", method = [RequestMethod.GET])
    fun getLoginUserInfo(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token: String
    ): DesignResponse<UserInfoRes> {
        return simpleUserInfoServiceImpl.getUserInfoByToken(token)
    }
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("info/update",  method = [RequestMethod.POST])
    fun updateUserInfo(@RequestBody updateUserInfoParam: UpdateUserInfoParams):DesignResponse<Boolean>{
        return simpleUserInfoServiceImpl.updateUserInfo(updateUserInfoParam)
    }


    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("info/search")
    fun searchUsersByKeywords(
        @RequestParam(name = "keywords") keywords:String,
        @RequestParam(name = "page", required = false) page:Int?=1,
        @RequestParam(name = "size", required = false) pageSize:Int?=20,
    ):DesignResponse<List<UserInfoRes>>{
        return simpleUserInfoServiceImpl.searchUsersByKeywords(keywords, page, pageSize)
    }

}