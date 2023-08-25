package xcj.app.appsets.im

object RabbitMqBrokerPropertyDesignType {
    const val TYPE_TEXT = "im.content.text"
    const val TYPE_IMAGE = "im.content.image"
    const val TYPE_VIDEO = "im.content.video"
    const val TYPE_MUSIC = "im.content.music"
    const val TYPE_VOICE = "im.content.voice"
    const val TYPE_LOCATION = "im.content.location"
    const val TYPE_FILE = "im.content.file"
    const val TYPE_HTML = "im.content.html"
    const val TYPE_AD = "im.content.ad"
    const val TYPE_SYSTEM = "im.content.system"
    const val TYPE_CUSTOM = "im.content.custom.*"
    fun getTypeByImMessage(imMessage: ImMessage): String {
        return when (imMessage) {
            is ImMessage.Text -> TYPE_TEXT
            is ImMessage.Image -> TYPE_IMAGE
            is ImMessage.Video -> TYPE_VIDEO
            is ImMessage.Music -> TYPE_MUSIC
            is ImMessage.Voice -> TYPE_VOICE
            is ImMessage.Location -> TYPE_LOCATION
            is ImMessage.File -> TYPE_FILE
            is ImMessage.HTML -> TYPE_HTML
            is ImMessage.Ad -> TYPE_AD
            is ImMessage.System -> TYPE_SYSTEM
            else -> TYPE_CUSTOM
        }
    }
}