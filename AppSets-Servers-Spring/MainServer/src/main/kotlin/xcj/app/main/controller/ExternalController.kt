package xcj.app.main.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.service.external.ExternalService
import xcj.app.main.service.external.TCObjectStorageRes

@RequestMapping("/appsets")
@RestController
class ExternalController(
    private val externalService: ExternalService
) {

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.TOKEN_MD5)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @RequestMapping("external/object-storage/config/{duration}")
    fun getObjectStorageConfig(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestHeader(name = ApiDesignKeys.APP_TOKEN_MD5) appToken: String,
        @PathVariable(name = "duration") duration: Long,
    ): DesignResponse<TCObjectStorageRes> {
        return externalService.getObjectStorageConfig(token, appToken, duration)
    }
}