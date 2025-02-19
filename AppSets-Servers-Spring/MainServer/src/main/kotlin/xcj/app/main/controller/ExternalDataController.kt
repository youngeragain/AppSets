package xcj.app.main.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.external.ExternalDataFetcherService

@RequestMapping("/appsets/data/external")
@RestController
class ExternalDataController(
    private val externalDataFetcherService: ExternalDataFetcherService
) {
    @ApiDesignPermission.VersionRequired(200)
    //@ApiDesignPermission.AdministratorRequired
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.TOKEN_MD5)
    @RequestMapping("/fetch")
    fun fetchAny(@RequestParam(name = "what") fetchWhat: Int): DesignResponse<Boolean> {
        return externalDataFetcherService.fetch(fetchWhat)
    }
}