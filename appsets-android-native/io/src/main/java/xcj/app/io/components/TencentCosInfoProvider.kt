package xcj.app.io.components

interface TencentCosInfoProvider {
    fun getTencentCosSTS(): TencentCosSTS
    fun getTencentCosRegionBucket(): TencentCosRegionBucket
}