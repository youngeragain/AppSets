package xcj.app.main.model.common

data class TencentCosSTS(
    val tmpSecretId: String,
    val tmpSecretKey: String,
    val sessionToken: String,
    val duration: Int,
    val serverTimeMills: Long
)