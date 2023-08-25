package xcj.app.userinfo.qr

import com.fasterxml.jackson.annotation.JsonProperty

data class BaiduHotData(
    val hotsearch: List<Hotsearch>
)

data class Hotsearch(
    @JsonProperty("cardTitle")
    val card_title: String,
    @JsonProperty("heatScore")
    val heat_score: String,
    val hotTags: String,
    val index: String,
    val isNew: String,
    val isViewed: String,
    val linkurl: String,
    @JsonProperty("preTag")
    val pre_tag: String,
    val views: String
)