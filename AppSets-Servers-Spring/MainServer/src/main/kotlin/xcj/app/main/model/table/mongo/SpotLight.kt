package xcj.app.main.model.table.mongo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import xcj.app.main.model.common.BaiduHotData
import xcj.app.main.model.common.MicrosoftBingWallpaper
import java.util.*

@Document("SpotLight")
data class SpotLight(
    @org.springframework.data.annotation.Id
    @JsonIgnore
    val _id: ObjectId,
    val holiday: Holiday? = null,
    val popular_searches: PopularSearches? = null,
    val today_in_history_list: List<TodayInHistory>? = null,
    val word_of_the_day_list: List<WordOfTheDay>? = null,
    val baidu_hot_data: BaiduHotData? = null,
    val bing_wallpaper: MicrosoftBingWallpaper? = null
)

data class Holiday(
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
    val pic_url: String?,
    val type: String? = null
)
