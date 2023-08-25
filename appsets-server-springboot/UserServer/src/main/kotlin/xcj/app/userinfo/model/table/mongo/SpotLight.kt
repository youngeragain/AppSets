package xcj.app.userinfo.model.table.mongo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import xcj.app.userinfo.qr.BaiduHotData
import xcj.app.userinfo.qr.MicrosoftBingWallpaperJson
import java.util.*

@Document("SpotLight")
data class SpotLight(
    @org.springframework.data.annotation.Id
    @JsonIgnore
    val _id: ObjectId,
    val holiday: Holiday?,
    @JsonProperty("popularSearches")
    val popular_searches: PopularSearches?,
    @JsonProperty("todayInHistory")
    val today_in_history: TodayInHistory?,
    @JsonProperty("wordOfTheDay")
    val word_of_the_day: WordOfTheDay?,
    @JsonProperty("baiduHotData")
    var baidu_hot_data: BaiduHotData?,
    @JsonProperty("bingWallpaperJson")
    var bing_wallpaper_json: MicrosoftBingWallpaperJson?
)

data class Holiday(
    @JsonProperty("infoUrl")
    val info_url: String?,
    @JsonProperty("moreUrl")
    val more_url: String?,
    val name: String?,
    @JsonProperty("picUrl")
    val pic_url: String?
)

data class PopularSearches(
    val keywords: List<String>?,
    val url: String?
)

data class TodayInHistory(
    val date: Date?,
    val event: String?,
    val title: String?,
    @JsonProperty("infoUrl")
    val info_url: String?,
    @JsonProperty("picUrl")
    val pic_url: String?
)

data class WordOfTheDay(
    val author: String?,
    @JsonProperty("authorInfo")
    val author_info: String?,
    @JsonProperty("infoUrl")
    val info_url: String?,
    val word: String?,
    @JsonProperty("picUrl")
    val pic_url:String?,
)
