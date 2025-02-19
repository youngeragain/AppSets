package xcj.app.appsets.server.model

import com.google.gson.annotations.SerializedName

data class Hotsearch(
    @SerializedName("cardTitle")
    val cardTitle: String,
    @SerializedName("heatScore")
    val heatScore: String,
    val hotTags: String,
    val index: String,
    val isNew: Boolean,
    val isViewed: String,
    val linkurl: String,
    @SerializedName("preTag")
    val preTag: String,
    val views: String
)

data class BaiduHotData(
    val hotsearch: List<Hotsearch>?
)