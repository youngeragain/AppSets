package xcj.app.userinfo.model.req

data class UpdateAppParams(
    val appId:String,
    val category: String?,
    val hasAd: String?,
    val iconUrl: String?,
    val introduction: String?,
    val name: String?,
    val price: Double?,
    val screenshots: List<String>?,
    val size: Int?,
    val version: String?,
    val versionCode: Int?,
    val updateChanges:String?,
    val bannerUrl:String?
)