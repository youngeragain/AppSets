package xcj.app.appsets.server.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SpotLight(
    @SerializedName("holiday")
    val holiday: Holiday?,
    @SerializedName("popularSearches")
    val popularSearches: PopularSearches?,
    @SerializedName("todayInHistoryList")
    val todayInHistoryList: List<TodayInHistory>?,
    @SerializedName("wordOfTheDayList")
    val wordOfTheDayList: List<WordOfTheDay>?,
    @SerializedName("baiduHotData")
    val baiduHotData: BaiduHotData?,
    @SerializedName("microsoftBingWallpaperList")
    val microsoftBingWallpaperList: List<MicrosoftBingWallpaper>?
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