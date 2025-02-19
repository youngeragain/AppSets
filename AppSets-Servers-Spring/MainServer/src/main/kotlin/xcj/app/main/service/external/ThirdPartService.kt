package xcj.app.main.service.external

import xcj.app.DesignResponse
import xcj.app.main.model.common.TencentCosRegionBucket
import xcj.app.main.model.common.TencentCosSTS

interface ThirdPartService {

    fun getTencentCosSTS(): DesignResponse<TencentCosSTS?>

    fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?>

}