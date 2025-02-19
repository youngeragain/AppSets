package xcj.app.main.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.common.TencentCosRegionBucket
import xcj.app.main.model.common.TencentCosSTS
import xcj.app.main.service.external.ThirdPartService

@RequestMapping("/appsets/thirdpart")
@RestController
class ThirdPartController(val thirdPartService: ThirdPartService) {
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @GetMapping("tencent/cos/sts")
    fun getTencentCosSTS(): DesignResponse<TencentCosSTS?> {
        return thirdPartService.getTencentCosSTS()
    }

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @GetMapping("tencent/cos/regionbucket")
    fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?> {
        return thirdPartService.getTencentCosRegionBucket()
    }
}