package xcj.app.io.tencent

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.foundation.http.DesignResponse

class ThirdPartRepository(private val thirdPartApi: ThirdPartApi) {
    suspend fun getTencentCosSTS(): DesignResponse<TencentCosSTS> = withContext(Dispatchers.IO) {
        return@withContext thirdPartApi.getTencentCosSTS()
    }

    suspend fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket> = withContext(
        Dispatchers.IO
    ) {
        return@withContext thirdPartApi.getTencentCosRegionBucket()
    }
}