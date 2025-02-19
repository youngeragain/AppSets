package xcj.app.main.im

object ImMessageDesignType {
    const val TYPE_TEXT = "im.text"
    const val TYPE_IMAGE = "im.image"
    const val TYPE_VIDEO = "im.video"
    const val TYPE_MUSIC = "im.music"
    const val TYPE_VOICE = "im.voice"
    const val TYPE_LOCATION = "im.location"
    const val TYPE_FILE = "im.file"
    const val TYPE_HTML = "im.html"
    const val TYPE_AD = "im.ad"
    const val TYPE_SYSTEM = "im.system"
    const val TYPE_CUSTOM = "im.custom.*"

    @JvmStatic
    fun getTypeByImMessage(imMessage: ImMessage): String {
        return TYPE_SYSTEM
    }
}