package xcj.app.appsets.ui.model

import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.DownloadInfo
import xcj.app.appsets.server.model.Platform
import xcj.app.appsets.server.model.ScreenshotInfo
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.appsets.util.model.UriProvider
import xcj.app.appsets.util.model.parseHttpUriHolder
import xcj.app.starter.android.util.PurpleLogger
import java.util.UUID

data class ApplicationForCreate(
    val appId: String = UUID.randomUUID().toString(),
    val iconUriHolder: UriProvider? = null,
    val bannerUriHolder: UriProvider? = null,
    val name: String = "",
    val category: String = "",
    val website: String = "",
    val developerInfo: String = "",
    val price: String = "",
    val priceUnit: String = "",
    val platformForCreates: List<PlatformForCreate> = emptyList()
) {
    companion object {
        private const val TAG = "ApplicationForCreate"
        const val PRICE_UNIT_RMB = "RMB"
        const val PRICE_UNIT_USD = "USD"
        const val DEFAULT_PRICE_UNIT = PRICE_UNIT_RMB

        const val CREATE_STEP_APPLICATION = "Application"
        const val CREATE_STEP_PLATFORM = "Platform"
        const val CREATE_STEP_VERSION = "Version"
        const val CREATE_STEP_SCREENSHOT = "ScreenShot"
        const val CREATE_STEP_DOWNLOAD = "Download"

        const val FILED_NAME_ICON_URI_HOLDER = "iconUriHolder"
        const val FILED_NAME_BANNER_URI_HOLDER = "bannerUriHolder"
        const val FILED_NAME_NAME = "name"
        const val FILED_NAME_CATEGORY = "category"
        const val FILED_NAME_WEBSITE = "website"
        const val FILED_NAME_DEVELOPER_INFO = "developerInfo"
        const val FILED_NAME_PRICE = "price"
        const val FILED_NAME_PRICE_UNIT = "priceUnit"

        fun inflateFromApplication(
            application: Application,
            applicationForCreate: ApplicationForCreate?
        ): ApplicationForCreate {
            PurpleLogger.current.d(TAG, "inflateFromApplication")
            if (applicationForCreate != null) {
                return applicationForCreate.copy(
                    appId = application.appId ?: "",
                    iconUriHolder = application.iconUrl?.toString().parseHttpUriHolder(),
                    bannerUriHolder = application.bannerUrl.parseHttpUriHolder(),
                    name = application.name ?: "",
                    category = application.category ?: "",
                    website = application.website ?: "",
                    developerInfo = application.developerInfo ?: "",
                    price = application.price ?: "",
                    priceUnit = application.priceUnit ?: "",
                    platformForCreates = application.platforms?.map(PlatformForCreate.Companion::inflateFromPlatform)
                        ?.toMutableList() ?: mutableListOf()
                )
            }
            return ApplicationForCreate(
                appId = application.appId ?: "",
                iconUriHolder = application.bioUrl?.toString().parseHttpUriHolder(),
                bannerUriHolder = application.bannerUrl.parseHttpUriHolder(),
                name = application.name ?: "",
                category = application.category ?: "",
                website = application.website ?: "",
                developerInfo = application.developerInfo ?: "",
                price = application.price ?: "",
                priceUnit = application.priceUnit ?: "",
                platformForCreates = application.platforms?.map(PlatformForCreate.Companion::inflateFromPlatform)
                    ?.toMutableList() ?: mutableListOf()
            )
        }
    }
}

data class PlatformForCreate(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val packageName: String = "",
    val introduction: String = "",
    val versionInfoForCreates: List<VersionInfoForCreate> = emptyList()
) {

    companion object {
        private const val TAG = "PlatformForCreate"

        const val FILED_NAME_NAME = "name"
        const val FILED_NAME_PACKAGE = "packageName"
        const val FILED_NAME_INTRODUCTION = "introduction"

        fun inflateFromPlatform(platForm: Platform): PlatformForCreate {
            PurpleLogger.current.d(TAG, "inflateFromPlatform")
            return PlatformForCreate(
                id = platForm.id ?: "",
                name = platForm.name ?: "",
                packageName = platForm.packageName ?: "",
                introduction = platForm.introduction ?: "",
                versionInfoForCreates = platForm.versionInfos?.map(VersionInfoForCreate.Companion::inflateFromVersionInfo)
                    ?.toMutableList() ?: mutableListOf()
            )
        }
    }
}

data class VersionInfoForCreate(
    val id: String = UUID.randomUUID().toString(),
    val version: String = "",
    val versionCode: String = "",
    val changes: String = "",
    val versionIconUriHolder: UriProvider? = null,
    val versionBannerUriHolder: UriProvider? = null,
    val packageSize: String = "",
    val privacyPolicyUrl: String = "",
    val screenshotInfoForCreates: List<ScreenshotInfoForCreate> = emptyList(),
    val downloadInfoForCreates: List<DownloadInfoForCreate> = emptyList()
) {
    companion object {
        private const val TAG = "VersionInfoForCreate"

        const val FILED_NAME_VERSION = "version"
        const val FILED_NAME_VERSION_CODE = "versionCode"
        const val FILED_NAME_CHANGES = "changes"
        const val FILED_NAME_ICON_URI_HOLDER = "versionIconUriHolder"
        const val FILED_NAME_BANNER_URI_HOLDER = "versionBannerUriHolder"
        const val FILED_NAME_PACKAGE_SIZE = "packageSize"
        const val FILED_NAME_PRIVACY_POLICY_URL = "privacyPolicyUrl"

        fun inflateFromVersionInfo(versionInfo: VersionInfo): VersionInfoForCreate {
            PurpleLogger.current.d(TAG, "inflateFromVersionInfo")
            return VersionInfoForCreate(
                versionIconUriHolder = versionInfo.versionIconUrl.parseHttpUriHolder(),
                versionBannerUriHolder = versionInfo.versionBannerUrl.parseHttpUriHolder(),
                id = versionInfo.id ?: "",
                version = versionInfo.version ?: "",
                versionCode = versionInfo.versionCode ?: "",
                changes = versionInfo.changes ?: "",
                packageSize = versionInfo.packageSize ?: "",
                privacyPolicyUrl = versionInfo.privacyUrl ?: "",
                screenshotInfoForCreates = versionInfo.screenshotInfos?.map(
                    ScreenshotInfoForCreate.Companion::inflateFromScreenshotInfo
                )?.toMutableList() ?: mutableListOf(),
                downloadInfoForCreates = versionInfo.downloadInfos?.map(
                    DownloadInfoForCreate.Companion::inflateFromDownloadInfo
                )?.toMutableList() ?: mutableListOf()
            )
        }
    }
}

/**
 * @param type video,image
 */
data class ScreenshotInfoForCreate(
    val id: String = UUID.randomUUID().toString(),
    val uriHolder: UriProvider? = null,
    val type: String? = null,
) {

    companion object {
        private const val TAG = "ScreenshotInfoForCreate"

        const val FILED_NAME_URI_HOLDER = "uriHolder"
        const val FILED_NAME_TYPE = "type"

        fun inflateFromScreenshotInfo(screenshotInfo: ScreenshotInfo): ScreenshotInfoForCreate {
            PurpleLogger.current.d(TAG, "inflateFromScreenshotInfo")
            return ScreenshotInfoForCreate(
                uriHolder = screenshotInfo.url.parseHttpUriHolder()
            )
        }
    }
}

data class DownloadInfoForCreate(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
) {
    companion object {
        private const val TAG = "DownloadInfoForCreate"

        const val FILED_NAME_URL = "url"

        fun inflateFromDownloadInfo(downloadInfo: DownloadInfo): DownloadInfoForCreate {
            PurpleLogger.current.d(TAG, "inflateFromDownloadInfo")
            return DownloadInfoForCreate(
                id = downloadInfo.id ?: "",
                url = downloadInfo.url?.let {
                    "********"
                } ?: ""
            )
        }
    }
}