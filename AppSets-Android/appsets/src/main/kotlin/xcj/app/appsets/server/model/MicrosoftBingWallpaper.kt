package xcj.app.appsets.server.model

data class MicrosoftBingWallpaper(
    val images: List<Image>?,
    val tooltips: Tooltips?
) {
    val url: String?
        get() = images?.getOrNull(0)?.let {
            "https://www.bing.com/${it.url?.replace("/", "")}"
        }
    val where
        get() = images?.getOrNull(0)?.copyright ?: "一个美好的地方"
    val whereBlowText
        get() = images?.getOrNull(0)?.title ?: "每日一题"
}

data class Image(
    val bot: Int?,
    val copyright: String?,
    val copyrightlink: String?,
    val drk: Int?,
    val enddate: String?,
    val fullstartdate: String?,
    val hs: List<Any>?,
    val hsh: String?,
    val quiz: String?,
    val startdate: String?,
    val title: String?,
    val top: Int?,
    val url: String?,
    val urlbase: String?,
    val wp: Boolean?
)

data class Tooltips(
    val loading: String?,
    val next: String?,
    val previous: String?,
    val walle: String?,
    val walls: String?
)