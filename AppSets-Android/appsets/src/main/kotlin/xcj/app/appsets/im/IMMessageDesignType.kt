package xcj.app.appsets.im

import xcj.app.appsets.im.message.AdMessage
import xcj.app.appsets.im.message.FileMessage
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.LocationMessage
import xcj.app.appsets.im.message.MusicMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.TextMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VoiceMessage

object IMMessageDesignType {

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
    fun getType(imMessage: IMMessage): String {
        return when (imMessage) {
            is TextMessage -> TYPE_TEXT
            is ImageMessage -> TYPE_IMAGE
            is VideoMessage -> TYPE_VIDEO
            is MusicMessage -> TYPE_MUSIC
            is VoiceMessage -> TYPE_VOICE
            is LocationMessage -> TYPE_LOCATION
            is FileMessage -> TYPE_FILE
            is HTMLMessage -> TYPE_HTML
            is AdMessage -> TYPE_AD
            is SystemMessage -> TYPE_SYSTEM
            else -> TYPE_CUSTOM
        }
    }
}