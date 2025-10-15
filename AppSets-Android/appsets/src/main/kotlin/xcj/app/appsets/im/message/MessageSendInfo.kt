package xcj.app.appsets.im.message

data class MessageSendInfo(
    val progress: Float = 0f,
    val isSent: Boolean = false,
    val failureReason: String? = null
)