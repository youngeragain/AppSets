package xcj.app.io.components

import xcj.app.core.foundation.http.DesignResponse

interface ThirdPartApi {
    suspend fun getTencentCosSTS(): DesignResponse<TencentCosSTS?>

    suspend fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?>
}




