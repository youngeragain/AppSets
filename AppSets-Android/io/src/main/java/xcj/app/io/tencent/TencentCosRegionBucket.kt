package xcj.app.io.tencent

import android.util.Base64

data class TencentCosRegionBucket(
    var region: String,
    var bucketName: String,
    var filePathPrefix: String
) {
    private var isDecode = false

    fun decode(): TencentCosRegionBucket {
        if (isDecode) {
            return this
        }
        runCatching {
            region = Base64.decode(region, Base64.DEFAULT).decodeToString()
            bucketName = Base64.decode(bucketName, Base64.DEFAULT).decodeToString()
            filePathPrefix = Base64.decode(filePathPrefix, Base64.DEFAULT).decodeToString()
        }
        isDecode = true
        return this
    }
}