package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission
import xcj.app.CoreLogger
import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.AddAppTokenParam
import xcj.app.userinfo.model.req.GetAppTokenParam
import xcj.app.userinfo.model.table.mongo.SpotLight
import xcj.app.userinfo.service.mongo.AddAppSetsVersionForPlatformParams
import xcj.app.userinfo.service.mongo.AppSetsService
import xcj.app.userinfo.service.mongo.SpotLightService
import xcj.app.userinfo.service.mongo.UpdateCheckResult

@RequestMapping("/appsets")
@RestController
class AppSetsController(
    private val appSetsService: AppSetsService,
    private val spotLightService: SpotLightService,
) {
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("apptoken/get", method = [RequestMethod.POST])
    fun getAppToken(@RequestBody getAppTokenParam: GetAppTokenParam):DesignResponse<String?>{
        return appSetsService.getTokenAppSetsAppId(getAppTokenParam)
    }
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("apptoken/create", method = [RequestMethod.POST])
    fun createAppSetsId(@RequestBody addAppTokenParams: AddAppTokenParam):DesignResponse<String>{
        return appSetsService.createAppSetsId(addAppTokenParams)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.platformStrToMd5)
    @GetMapping("client/update")
    fun applicationClientCheckUpdateV2(
        @RequestParam("versionCode") versionCode:Int,
        @RequestHeader(ApiDesignEncodeStr.platformStrToMd5) platform:String
    ):DesignResponse<UpdateCheckResult?>{
        return appSetsService.appsetsClientCheckUpdate(versionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.platformStrToMd5)
    @GetMapping("application/client/update")
    fun applicationClientCheckUpdateV1(
        @RequestParam("versionCode") versionCode:Int,
        @RequestHeader("platform") platform:String
    ):DesignResponse<UpdateCheckResult?>{
        return appSetsService.appsetsClientCheckUpdate(versionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @PostMapping("client/update")
    fun addApplicationClientUpdate(
        @RequestBody addAppSetsVersionForPlatformParams: AddAppSetsVersionForPlatformParams
    ):DesignResponse<Boolean>{
        return appSetsService.addAppSetsClientUpdate(addAppSetsVersionForPlatformParams)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @GetMapping("client/update/history")
    fun getApplicationClientUpdateHistory(
        @RequestHeader("platform") platform:String,
        @RequestParam(name = "min", required = false) minVersionCode:Int? = null
    ):DesignResponse<List<UpdateCheckResult>?>{
        return appSetsService.getApplicationClientUpdateHistory(minVersionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.platformStrToMd5)
    @GetMapping("client/update/history2")
    fun getApplicationClientUpdateHistoryV2(
        @RequestHeader(ApiDesignEncodeStr.platformStrToMd5) platform:String,
        @RequestParam(name = "min", required = false) minVersionCode:Int? = null
    ):DesignResponse<List<UpdateCheckResult>?>{
        return appSetsService.getApplicationClientUpdateHistory(minVersionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @RequestMapping("win11searchspotlight", method = [RequestMethod.GET])
    fun getWin11SpotLightInfo():DesignResponse<SpotLight>{
        return spotLightService.getWin11SpotLightInfo()
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @RequestMapping("spotlight", method = [RequestMethod.GET])
    fun getWin11SpotLightInfoV2():DesignResponse<SpotLight>{
        return spotLightService.getWin11SpotLightInfo()
    }


}