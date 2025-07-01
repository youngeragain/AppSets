package xcj.app.main.model.table.mongo

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field

@Document("Application")
data class Application(
    @Id
    val id: ObjectId?,
    @Field("icon_url")
    val iconUrl: String?,
    val website: String?,
    @Field("update_time")
    val updateTime: String?,
    @Field("create_time")
    val createTime: String?,
    @Field("developer_info")
    val developerInfo: String?,
    @Field("price")
    val price: String?,
    @Field("price_unit")
    val priceUnit: String?,
    @Field("banner_url")
    val bannerUrl: String?,
    @Field("create_uid")
    val createUid: String?,
    @Field("update_uid")
    val updateUid: String?,
    val name: String?,
    val category: String?,
    @Field("app_id")
    val appId: String?,
    val platforms: List<AppPlatform>?
)

data class AppPlatform(
    @Field("id")
    val id: String?,
    val name: String?,
    @Field("package_name")
    val packageName: String?,
    val introduction: String?,
    @Field("version_infos")
    val versionInfos: List<VersionInfo>?
)

data class VersionInfo(
    @Field("id")
    val id: String?,
    @Field("version_icon_url")
    val versionIconUrl: String?,
    @Field("version_banner_url")
    val versionBannerUrl: String?,
    val version: String?,
    @Field("version_code")
    val versionCode: String?,
    val changes: String?,
    @Field("package_size")
    val packageSize: String?,
    @Field("privacy_url")
    val privacyUrl: String?,
    @Field("screenshot_infos")
    val screenshotInfos: List<ScreenshotInfo>?,
    @Field("download_infos")
    val downloadInfos: List<DownloadInfo>?
)

data class ScreenshotInfo(
    @Field("id")
    val id: String?,
    @Field("create_uid")
    val createUid: String?,
    @Field("update_uid")
    val updateUid: String?,
    @Field("create_time")
    val createTime: String?,
    @Field("update_time")
    val updateTime: String?,
    val type: String?,
    @Field("content_type")
    val contentType: String?,
    val url: String?,
)

data class DownloadInfo(
    @Field("id")
    val id: String?,
    @Field("create_uid")
    val createUid: String?,
    @Field("update_uid")
    val updateUid: String?,
    @Field("create_time")
    val createTime: String?,
    @Field("update_time")
    val updateTime: String?,
    @Field("download_times")
    val downloadTimes: Int?,
    val url: String?,
    val description: String?,
    val architectures: List<String>?,
    val size: String?,
)

