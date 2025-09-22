package xcj.app.io.tencent

interface TencentCosInfoProvider {
    suspend fun getTencentCosSTS(): TencentCosSTS?
    suspend fun getTencentCosRegionBucket(): TencentCosRegionBucket?
}