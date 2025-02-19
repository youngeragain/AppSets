package xcj.app.main.qr

/**
 * @param providerId 二维码提供者id
 * @param code 使用Atom生成的
 * @param providerInfo 二维码提供者信息
 * @param scannerInfo 扫描者信息
 */
data class QRCodeInfoInRedis(
    val providerId: String,
    val code: String,
    var state: String,
    var info: String,
    val providerInfo: String,
    var scannerInfo: String? = null,
    var extra: String? = null
)