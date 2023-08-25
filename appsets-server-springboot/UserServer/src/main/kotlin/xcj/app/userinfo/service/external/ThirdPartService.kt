package xcj.app.userinfo.service.external

import xcj.app.DesignResponse
import xcj.app.userinfo.model.common.TencentCosRegionBucket
import xcj.app.userinfo.model.common.TencentCosSTS

interface ThirdPartService{
    fun getTencentCosSTS(): DesignResponse<TencentCosSTS?>
    fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?>
}