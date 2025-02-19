package xcj.app.appsets.server.api

import retrofit2.http.GET
import xcj.app.io.tencent.TencentCosRegionBucket
import xcj.app.io.tencent.TencentCosSTS
import xcj.app.io.tencent.ThirdPartApi
import xcj.app.starter.foundation.http.DesignResponse

interface ThirdPartApi1 : ThirdPartApi {
    @GET("appsets/thirdpart/tencent/cos/sts")
    override suspend fun getTencentCosSTS(): DesignResponse<TencentCosSTS>

    @GET("appsets/thirdpart/tencent/cos/regionbucket")
    override suspend fun getTencentCosRegionBucket(): DesignResponse<TencentCosRegionBucket>
}