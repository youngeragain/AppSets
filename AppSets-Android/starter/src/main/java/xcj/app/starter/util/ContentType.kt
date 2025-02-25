package xcj.app.starter.util

object ContentType {
    const val ALL = "*/*"
    const val APPLICATION_FILE = "application/file"
    const val APPLICATION_TEXT = "application/text"
    const val APPLICATION_BYTES = "application/bytes"
    const val APPLICATION_JSON = "application/json"
    const val APPLICATION_GEO = "application/geo"
    const val APPLICATION_XML = "application/xml"
    const val APPLICATION_JAVASCRIPT = "application/javascript"
    const val APPLICATION_PDF = "application/pdf"
    const val APPLICATION_OCTET_STREAM = "application/octet-stream"
    const val APPLICATION_FORM_DATA = "application/x-www-form-urlencoded"
    const val APPLICATION_ANDROID_PACKAGE = "application/vnd.android.package-archive"

    const val IMAGE = "image/*"
    const val IMAGE_PREFIX = "image/"
    const val VIDEO = "video/*"
    const val VIDEO_PREFIX = "video/"
    const val AUDIO = "audio/*"
    const val AUDIO_PREFIX = "audio/"
    const val AUDIO_MUSIC = "audio/music"

    const val MULTIPART_FORM_DATA = "multipart/form-data"
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_HTML = "text/html"
    const val TEXT_CSS = "text/css"

    const val APPSETS_SHARE_SYSTEM = "appsets/share/sys"
    const val APPSETS_SHARE_SYSTEM_SERVER_SEND_CLIENT_IP_AND_SELF_NAME = "appsets/share/c_ip_s_name"
    const val APPSETS_SHARE_SYSTEM_CLIENT_SEND_IP_AND_SELF_NAME = "appsets/share/c_ip_c_name"

    fun isImage(contentType: String): Boolean {
        return contentType.startsWith(IMAGE_PREFIX)
    }

    fun isVideo(contentType: String): Boolean {
        return contentType.startsWith(VIDEO_PREFIX)
    }

    fun isAudio(contentType: String): Boolean {
        return contentType.startsWith(AUDIO_PREFIX)
    }


}