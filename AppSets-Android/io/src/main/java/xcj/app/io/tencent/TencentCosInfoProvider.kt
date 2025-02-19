package xcj.app.io.tencent

interface TencentCosInfoProvider {
    fun getTencentCosSTS(): TencentCosSTS?
    fun getTencentCosRegionBucket(): TencentCosRegionBucket?
}