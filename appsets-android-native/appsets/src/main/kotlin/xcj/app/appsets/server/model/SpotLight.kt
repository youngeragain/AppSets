package xcj.app.appsets.server.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SpotLight(
    val holiday: Holiday?,
    @SerializedName("popularSearches")
    val popularSearches: PopularSearches?,
    @SerializedName("todayInHistory")
    val todayInHistory: TodayInHistory?,
    @SerializedName("wordOfTheDay")
    val wordOfTheDay: WordOfTheDay?,
    @SerializedName("baiduHotData")
    val baiduHotData: BaiduHotData?,
    @SerializedName("bingWallpaperJson")
    val bingWallpaperJson: MicrosoftBingWallpaperJson?
)

data class Holiday(
    @SerializedName("infoUrl")
    val infoUrl: String?,
    @SerializedName("moreUrl")
    val moreUrl: String?,
    val name: String?,
    @SerializedName("picUrl")
    val picUrl: String?
)

data class PopularSearches(
    val keywords: List<String>?,
    val url: String?
)

data class TodayInHistory(
    val date: Date?,
    val event: String?,
    @SerializedName("infoUrl")
    val infoUrl: String?,
    @SerializedName("picUrl")
    val picUrl: String?,
    val title: String
)

data class WordOfTheDay(
    val author: String?,
    @SerializedName("authorInfo")
    val authorInfo: String?,
    @SerializedName("infoUrl")
    val infoUrl: String?,
    val word: String?,
    @SerializedName("picUrl")
    val picUrl: String?
)