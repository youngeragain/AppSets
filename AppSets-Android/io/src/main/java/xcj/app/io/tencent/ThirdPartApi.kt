package xcj.app.io.tencent

import xcj.app.starter.foundation.http.DesignResponse

interface ThirdPartApi {
    suspend fun getTencentCosSTS(): DesignResponse<TencentCosSTS>

    suspend fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket>
}




