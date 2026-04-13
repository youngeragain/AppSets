package xcj.app.appsets.server.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import xcj.app.appsets.im.Bio

@Parcelize
data class Application(
    val appId: String,
    var iconUrl: String? = null,
    val website: String? = null,
    val updateTime: String? = null,
    val createTime: String? = null,
    val developerInfo: String? = null,
    val price: String? = null,
    val priceUnit: String? = null,
    var bannerUrl: String? = null,
    val createUid: String? = null,
    val updateUid: String? = null,
    val name: String? = null,
    val category: String? = null,
    val platforms: List<AppPlatform>? = null,
) : Bio {

    override val bioId: String
        get() = "$BIO_ID_PREFIX${appId}"

    override val bioName: String?
        get() = name

    override var bioUrl: Any? = null

    var currentVisiblePlatformPosition: Int = -1

    fun platformVersionDownloadInfos(platformName: String?): List<DownloadInfo> {
        if (platformName.isNullOrEmpty()) {
            return emptyList()
        }
        if (platforms.isNullOrEmpty()) {
            return emptyList()
        }
        platforms.forEach { platform ->
            if (platform.name == platformName) {
                return if (platform.versionInfos.isNullOrEmpty()) {
                    emptyList()
                } else {
                    platform.versionInfos.flatMap {
                        val downloadInfos = it.downloadInfos
                        if (downloadInfos.isNullOrEmpty()) {
                            emptyList()
                        } else {
                            downloadInfos
                        }
                    }
                }
            }
        }
        return emptyList()
    }

    fun hasPlatformDownloadInfo(platformName: String?): Boolean {
        if (platformName.isNullOrEmpty()) {
            return false
        }
        if (platforms.isNullOrEmpty()) {
            return false
        }
        platforms.forEach { platform ->
            if (platform.name == platformName) {
                if (platform.versionInfos.isNullOrEmpty()) {
                    return false
                } else {
                    platform.versionInfos.forEach { versionInfo ->
                        if (!versionInfo.downloadInfos.isNullOrEmpty()) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }


    companion object {

        const val BIO_ID_PREFIX = "APP-"

        fun basic(id: String, name: String?, iconUrl: String?): Application {
            return Application(appId = id, name = name, iconUrl = iconUrl).apply {
                bioUrl = iconUrl
            }
        }
    }
}

@Parcelize
data class AppPlatform(
    val id: String? = null,
    val name: String? = null,
    val packageName: String? = null,
    val introduction: String? = null,
    val versionInfos: List<VersionInfo>? = null,
) : Parcelable


@Parcelize
data class VersionInfo(
    val id: String? = null,
    var versionIconUrl: String? = null,
    var versionBannerUrl: String? = null,
    val version: String? = null,
    val versionCode: String? = null,
    val changes: String? = null,
    val packageSize: String? = null,
    val privacyUrl: String? = null,
    val screenshotInfos: List<ScreenshotInfo>? = null,
    val downloadInfos: List<DownloadInfo>? = null,
) : Parcelable

@Parcelize
data class ScreenshotInfo(
    val id: String? = null,
    val createUid: String? = null,
    val updateUid: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val type: String? = null,
    val contentType: String? = null,
    var url: String? = null,
) : Parcelable

@Parcelize
data class DownloadInfo(
    val id: String? = null,
    val createUid: String? = null,
    val updateUid: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val downloadTimes: Int?,
    val url: String? = null,
    val description: String? = null,
    val architectures: List<String>? = null,
    val size: String? = null,
) : Parcelable
