package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.common.AddAppSetsVersionForPlatformParams
import xcj.app.main.model.common.UpdateCheckResult
import xcj.app.main.model.req.AddAppTokenParam
import xcj.app.main.model.req.GetAppTokenParam
import xcj.app.main.model.res.MediaContentRes
import xcj.app.main.model.res.SpotLightRes
import xcj.app.main.service.MediaContentService
import xcj.app.main.service.mongo.AppSetsService
import xcj.app.main.service.mongo.SpotLightService

@RequestMapping("/appsets")
@RestController
class AppSetsController(
    private val appSetsService: AppSetsService,
    private val spotLightService: SpotLightService,
    private val mediaContentService: MediaContentService
) {
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("apptoken/get", method = [RequestMethod.POST])
    fun getAppToken(@RequestBody getAppTokenParam: GetAppTokenParam): DesignResponse<String?> {
        return appSetsService.getTokenAppSetsAppId(getAppTokenParam)
    }

    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("apptoken/create", method = [RequestMethod.POST])
    fun createAppSetsId(@RequestBody addAppTokenParams: AddAppTokenParam): DesignResponse<String> {
        return appSetsService.createAppSetsId(addAppTokenParams)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.PLATFORM_MD5)
    @GetMapping("client/update")
    fun applicationClientCheckUpdateV2(
        @RequestParam("versionCode") versionCode: Int,
        @RequestHeader(ApiDesignKeys.PLATFORM_MD5) platform: String
    ): DesignResponse<UpdateCheckResult?> {
        return appSetsService.appsetsClientCheckUpdate(versionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.PLATFORM_MD5)
    @GetMapping("application/client/update")
    fun applicationClientCheckUpdateV1(
        @RequestParam("versionCode") versionCode: Int,
        @RequestHeader("platform") platform: String
    ): DesignResponse<UpdateCheckResult?> {
        return appSetsService.appsetsClientCheckUpdate(versionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @PostMapping("client/update")
    fun addApplicationClientUpdate(
        @RequestBody addAppSetsVersionForPlatformParams: AddAppSetsVersionForPlatformParams
    ): DesignResponse<Boolean> {
        return appSetsService.addAppSetsClientUpdate(addAppSetsVersionForPlatformParams)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @PutMapping("client/update")
    fun updateApplicationClientUpdate(
        @RequestBody addAppSetsVersionForPlatformParams: AddAppSetsVersionForPlatformParams
    ): DesignResponse<Boolean> {
        return appSetsService.updateAppSetsClientUpdate(addAppSetsVersionForPlatformParams)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @GetMapping("client/update/history")
    fun getApplicationClientUpdateHistory(
        @RequestHeader("platform") platform: String,
        @RequestParam(name = "min", required = false) minVersionCode: Int? = null
    ): DesignResponse<List<UpdateCheckResult>?> {
        return appSetsService.getApplicationClientUpdateHistory(minVersionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.PLATFORM_MD5)
    @GetMapping("client/update/history2")
    fun getApplicationClientUpdateHistoryV2(
        @RequestHeader(ApiDesignKeys.PLATFORM_MD5) platform: String,
        @RequestParam(name = "min", required = false) minVersionCode: Int? = null
    ): DesignResponse<List<UpdateCheckResult>?> {
        return appSetsService.getApplicationClientUpdateHistory(minVersionCode, platform)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @RequestMapping("spotlight", method = [RequestMethod.GET])
    fun getSpotLightInfo(): DesignResponse<SpotLightRes> {
        return spotLightService.getSpotLightInfo()
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @RequestMapping("media_content/{type}", method = [RequestMethod.GET])
    fun getMediaContent(
        @PathVariable(name = "type") contentType: String,
        @RequestParam(name = "page", required = false, defaultValue = "1") page: Int,
        @RequestParam(name = "size", required = false, defaultValue = "10") pageSize: Int
    ): DesignResponse<List<MediaContentRes>> {
        return mediaContentService.getMediaContent(contentType, page, pageSize)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @GetMapping("/client_settings/im_broker_properties")
    fun getIMBrokerProperties(): DesignResponse<String?> {
        return appSetsService.getIMBrokerProperties()
    }
}