package xcj.app.appsets.server.api

import retrofit2.http.GET
import xcj.app.core.foundation.http.DesignResponse
import xcj.app.io.components.TencentCosRegionBucket
import xcj.app.io.components.TencentCosSTS
import xcj.app.io.components.ThirdPartApi

interface ThirdPartApi1 : ThirdPartApi, URLApi {
    @GET("appsets/thirdpart/tencent/cos/sts")
    override suspend fun getTencentCosSTS(): DesignResponse<TencentCosSTS?>

    @GET("appsets/thirdpart/tencent/cos/regionbucket")
    override suspend fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket?>
}