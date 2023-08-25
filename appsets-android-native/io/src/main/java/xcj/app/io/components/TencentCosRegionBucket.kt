package xcj.app.io.components

import android.util.Base64

/**
 * 服务器返回的为base64 encode
 */
data class TencentCosRegionBucket(
    var region: String,
    var bucketName: String,
    var filePathPrefix: String
) {
    fun decode() {
        region = Base64.decode(region, Base64.DEFAULT).decodeToString()
        bucketName = Base64.decode(bucketName, Base64.DEFAULT).decodeToString()
        filePathPrefix = Base64.decode(filePathPrefix, Base64.DEFAULT).decodeToString()
    }
}