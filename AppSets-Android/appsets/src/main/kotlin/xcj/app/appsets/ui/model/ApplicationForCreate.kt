package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.server.model.AppPlatform
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.DownloadInfo
import xcj.app.appsets.server.model.ScreenshotInfo
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.android.util.UriProvider
import java.util.UUID

data class ApplicationForCreate(
    var appId: String = UUID.randomUUID().toString(),
    val iconUriProvider: MutableState<UriProvider?> = mutableStateOf(null),
    val bannerUriProvider: MutableState<UriProvider?> = mutableStateOf(null),
    val name: MutableState<String> = mutableStateOf(""),
    val category: MutableState<String> = mutableStateOf(""),
    val website: MutableState<String> = mutableStateOf(""),
    val developerInfo: MutableState<String> = mutableStateOf(""),
    val price: MutableState<String> = mutableStateOf(""),
    val priceUnit: MutableState<String> = mutableStateOf(""),
    val platformForCreates: MutableList<PlatformForCreate> = mutableStateListOf()
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

        const val FILED_NAME_ICON_URI_HOLDER = "iconUriProvider"
        const val FILED_NAME_BANNER_URI_HOLDER = "bannerUriProvider"
        const val FILED_NAME_NAME = "name"
        const val FILED_NAME_CATEGORY = "category"
        const val FILED_NAME_WEBSITE = "website"
        const val FILED_NAME_DEVELOPER_INFO = "developerInfo"
        const val FILED_NAME_PRICE = "price"
        const val FILED_NAME_PRICE_UNIT = "priceUnit"

        fun inflateFromApplication(
            applicationForCreate: ApplicationForCreate,
            application: Application
        ) {
            PurpleLogger.current.d(TAG, "inflateFromApplication")
            applicationForCreate.apply {
                appId = application.appId
                iconUriProvider.value = UriProvider.fromString(application.bioUrl?.toString())
                bannerUriProvider.value = UriProvider.fromString(application.bannerUrl)
                name.value = application.bioName ?: ""
                category.value = application.category ?: ""
                website.value = application.website ?: ""
                developerInfo.value = application.developerInfo ?: ""
                price.value = application.price ?: ""
                priceUnit.value = application.priceUnit ?: ""
                application.platforms?.map(PlatformForCreate.Companion::inflateFromPlatform)?.let {
                    platformForCreates.addAll(it)
                }
            }
        }
    }
}

data class PlatformForCreate(
    val id: String = UUID.randomUUID().toString(),
    val name: MutableState<String> = mutableStateOf(""),
    val packageName: MutableState<String> = mutableStateOf(""),
    val introduction: MutableState<String> = mutableStateOf(""),
    val versionInfoForCreates: MutableList<VersionInfoForCreate> = mutableStateListOf()
) {

    companion object {
        private const val TAG = "PlatformForCreate"

        const val FILED_NAME_NAME = "name"
        const val FILED_NAME_PACKAGE = "packageName"
        const val FILED_NAME_INTRODUCTION = "introduction"

        fun inflateFromPlatform(platForm: AppPlatform): PlatformForCreate {
            PurpleLogger.current.d(TAG, "inflateFromPlatform")
            return PlatformForCreate(
                id = platForm.id ?: ""
            ).apply {
                name.value = platForm.name ?: ""
                packageName.value = platForm.packageName ?: ""
                introduction.value = platForm.introduction ?: ""
                platForm.versionInfos?.map(VersionInfoForCreate.Companion::inflateFromVersionInfo)
                    ?.let {
                        versionInfoForCreates.addAll(it)
                    }
            }
        }
    }
}

data class VersionInfoForCreate(
    val id: String = UUID.randomUUID().toString(),
    val version: MutableState<String> = mutableStateOf(""),
    val versionCode: MutableState<String> = mutableStateOf(""),
    val changes: MutableState<String> = mutableStateOf(""),
    val versionIconUriProvider: MutableState<UriProvider?> = mutableStateOf(null),
    val versionBannerUriProvider: MutableState<UriProvider?> = mutableStateOf(null),
    val packageSize: MutableState<String> = mutableStateOf(""),
    val privacyPolicyUrl: MutableState<String> = mutableStateOf(""),
    val screenshotInfoForCreates: MutableList<ScreenshotInfoForCreate> = mutableStateListOf(),
    val downloadInfoForCreates: MutableList<DownloadInfoForCreate> = mutableStateListOf()
) {
    companion object {
        private const val TAG = "VersionInfoForCreate"

        const val FILED_NAME_VERSION = "version"
        const val FILED_NAME_VERSION_CODE = "versionCode"
        const val FILED_NAME_CHANGES = "changes"
        const val FILED_NAME_ICON_URI_HOLDER = "versionIconUriProvider"
        const val FILED_NAME_BANNER_URI_HOLDER = "versionBannerUriProvider"
        const val FILED_NAME_PACKAGE_SIZE = "packageSize"
        const val FILED_NAME_PRIVACY_POLICY_URL = "privacyPolicyUrl"

        fun inflateFromVersionInfo(versionInfo: VersionInfo): VersionInfoForCreate {
            PurpleLogger.current.d(TAG, "inflateFromVersionInfo")
            return VersionInfoForCreate(
                id = versionInfo.id ?: ""
            ).apply {
                versionIconUriProvider.value =
                    UriProvider.fromString(versionInfo.versionIconUrl)
                versionBannerUriProvider.value =
                    UriProvider.fromString(versionInfo.versionBannerUrl)
                version.value = versionInfo.version ?: ""
                versionCode.value = versionInfo.versionCode ?: ""
                changes.value = versionInfo.changes ?: ""
                packageSize.value = versionInfo.packageSize ?: ""
                privacyPolicyUrl.value = versionInfo.privacyUrl ?: ""
                versionInfo.screenshotInfos?.map(
                    ScreenshotInfoForCreate.Companion::inflateFromScreenshotInfo
                )?.let {
                    screenshotInfoForCreates.addAll(it)
                }
                versionInfo.downloadInfos?.map(
                    DownloadInfoForCreate.Companion::inflateFromDownloadInfo
                )?.let {
                    downloadInfoForCreates.addAll(it)
                }
            }
        }
    }
}

/**
 * @param type video, image
 */
data class ScreenshotInfoForCreate(
    val id: String = UUID.randomUUID().toString(),
    val type: String? = null,
    val pictureUriProvider: MutableState<UriProvider?> = mutableStateOf(null),
) {

    companion object {
        private const val TAG = "ScreenshotInfoForCreate"

        const val FILED_NAME_URI_HOLDER = "uriProvider"
        const val FILED_NAME_TYPE = "type"

        fun inflateFromScreenshotInfo(screenshotInfo: ScreenshotInfo): ScreenshotInfoForCreate {
            PurpleLogger.current.d(TAG, "inflateFromScreenshotInfo")
            return ScreenshotInfoForCreate().apply {
                pictureUriProvider.value = UriProvider.fromString(screenshotInfo.url)
            }
        }
    }
}

data class DownloadInfoForCreate(
    val id: String = UUID.randomUUID().toString(),
    val url: MutableState<String> = mutableStateOf(""),
) {
    companion object {
        private const val TAG = "DownloadInfoForCreate"

        const val FILED_NAME_URL = "url"

        fun inflateFromDownloadInfo(downloadInfo: DownloadInfo): DownloadInfoForCreate {
            PurpleLogger.current.d(TAG, "inflateFromDownloadInfo")
            return DownloadInfoForCreate(
                id = downloadInfo.id ?: ""
            ).apply {
                url.value = downloadInfo.url?.let {
                    "********"
                } ?: ""
            }
        }
    }
}