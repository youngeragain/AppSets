package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.userinfo.model.common.TencentCosRegionBucket
import xcj.app.userinfo.model.common.TencentCosSTS
import xcj.app.userinfo.service.external.ThirdPartService

@RequestMapping("/appsets/thirdpart")
@RestController
class ThirdPartController(val thirdPartService: ThirdPartService) {
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @GetMapping("tencent/cos/sts")
    fun getTencentCosSTS():DesignResponse<TencentCosSTS?>{
        return thirdPartService.getTencentCosSTS()
    }

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @GetMapping("tencent/cos/regionbucket")
    fun getTencentCosRegionBucket():DesignResponse<TencentCosRegionBucket?>{
        return thirdPartService.getTencentCosRegionBucket()
    }
}