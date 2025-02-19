package xcj.app.main.model.req

data class AddAppParams(
    val platform: String?,
    val category: String?,
    val hasAd: String?,
    val iconUrl: String?,
    val introduction: String?,
    val name: String?,
    val packageName: String?,
    val price: Double?,
    val screenshots: List<String>?,
    val size: Int?,
    val version: String?,
    val versionCode: Int?,
    val bannerUrl: String?
)