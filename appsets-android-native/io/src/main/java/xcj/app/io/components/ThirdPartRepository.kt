package xcj.app.io.components

import xcj.app.core.foundation.http.DesignResponse

class ThirdPartRepository(private val thirdPartApi: ThirdPartApi) {
    suspend fun getTencentCosSTS(): DesignResponse<TencentCosSTS?> {
        return thirdPartApi.getTencentCosSTS()
    }

    suspend fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?> {
        return thirdPartApi.getTencentCosRegionBucket()
    }
}