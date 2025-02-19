package xcj.app.io.tencent

data class TencentCosSTS(
    val tmpSecretId: String,
    val tmpSecretKey: String,
    val sessionToken: String,
    val duration: Int,
    val serverTimeMills: Long
) {
    fun isOutOfDate(): Boolean {
        return (serverTimeMills / 1000 + duration) < (System.currentTimeMillis() / 1000)
    }
}