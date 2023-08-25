package xcj.app.appsets.usecase.models

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.ui.nonecompose.base.UriHolder

class ApplicationForCreate {
    var appId: String? = null
    val iconUriHolderState: MutableState<UriHolder?> = mutableStateOf(null)
    val bannerUriHolderState: MutableState<UriHolder?> = mutableStateOf(null)
    val name: MutableState<String> = mutableStateOf("")
    val category: MutableState<String> = mutableStateOf("")
    val website: MutableState<String> = mutableStateOf("")
    val developerInfo: MutableState<String> = mutableStateOf("")
    val platformForCreates: MutableList<PlatformForCreate> = mutableStateListOf()
    override fun toString(): String {
        return "App(appId=$appId, iconUrl=${iconUriHolderState.value}, name=${name.value}, category=${category.value} platforms=$platformForCreates)"
    }

    fun getPlatformForCreateById(platformId: String): PlatformForCreate? {
        val platformForCreate = platformForCreates.firstOrNull { it.id == platformId }
        Log.e("ApplicationForCreate", "getPlatformForCreateById:${platformForCreate}")
        return platformForCreate
    }

    fun getPlatformForCreateByName(platformName: String): PlatformForCreate {
        if (platformName.isEmpty())
            throw Exception()
        return getOrCreatePlatformIfNeeded(platformName)
    }

    private fun getOrCreatePlatformIfNeeded(platformName: String): PlatformForCreate {
        val platform = platformForCreates.firstOrNull { it.name == platformName }
        if (platform != null) {
            return platform
        }
        val platformForCreate = PlatformForCreate().apply {
            name = platformName
        }
        platformForCreates.add(platformForCreate)
        return platformForCreate
    }

    fun removePlatformForCreateByName(platformName: String) {
        platformForCreates.removeIf { it.name == platformName }
    }

    fun getLastPlatformForCreateOrNull(): PlatformForCreate? {
        return platformForCreates.lastOrNull()
    }

    fun inflateFromApplication(application: Application) {
        Log.e("ApplicationForCreate", "inflateFromApplication")
        application.iconUrl?.let {
            iconUriHolderState.value = object : UriHolder {
                override fun provideUri(): Uri? {
                    return Uri.parse(it)
                }

                override fun isLocalUri(): Boolean = false
            }
        }
        application.bannerUrl?.let {
            bannerUriHolderState.value = object : UriHolder {
                override fun provideUri(): Uri? {
                    return Uri.parse(it)
                }

                override fun isLocalUri(): Boolean = false
            }
        }
        application.appId?.let {
            appId = it
        }
        application.name?.let {
            name.value = it
        }
        application.category?.let {
            category.value = it
        }
        application.website?.let {
            website.value = it
        }
        application.developerInfo?.let {
            developerInfo.value = it
        }
        application.platforms?.forEach {
            val platformForCreate = PlatformForCreate()
            platformForCreate.inflateFromPlatform(it)
            platformForCreates.add(platformForCreate)
        }
    }

}

class PlatformForCreate {
    var id: String? = null
    var name: String = ""
    val packageName: MutableState<String> = mutableStateOf("")
    val introduction: MutableState<String> = mutableStateOf("")
    val versionInfoForCreates: MutableList<VersionInfoForCreate> = mutableStateListOf()
    override fun toString(): String {
        return "Platform(name='$name', packageName=${packageName.value}, introduction=${introduction.value}, versionInfos=$versionInfoForCreates)"
    }

    fun getVersionInfoForCreateById(versionInfoId: String): VersionInfoForCreate? {
        return versionInfoForCreates.firstOrNull { it.id == versionInfoId }
    }

    fun addVersionInfoForCreate() {
        val versionInfoForCreate = VersionInfoForCreate()
        versionInfoForCreates.add(versionInfoForCreate)
    }

    fun inflateFromPlatform(platForm: PlatForm) {
        Log.e("PlatformForCreate", "inflateFromPlatform")
        platForm.id?.let {
            id = it
        }
        platForm.name?.let {
            name = it
        }
        platForm.packageName?.let {
            packageName.value = it
        }
        platForm.introduction?.let {
            introduction.value = it
        }
        platForm.versionInfos?.forEach {
            val versionInfoForCreate = VersionInfoForCreate()
            versionInfoForCreate.inflateFromVersionInfo(it)
            versionInfoForCreates.add(versionInfoForCreate)
        }
    }
}

class VersionInfoForCreate {
    var id: String? = null
    val version: MutableState<String> = mutableStateOf("")
    val versionCode: MutableState<String> = mutableStateOf("")
    val changes: MutableState<String> = mutableStateOf("")
    val versionIconUriHolderState: MutableState<UriHolder?> = mutableStateOf(null)
    val versionBannerUriHolderState: MutableState<UriHolder?> = mutableStateOf(null)
    val packageSize: MutableState<String> = mutableStateOf("")
    val privacyPolicyUrl: MutableState<String> = mutableStateOf("")
    val screenshotInfoForCreates: MutableList<ScreenshotInfoForCreate> = mutableStateListOf()
    val downloadInfoForCreates: MutableList<DownloadInfoForCreate> = mutableStateListOf()
    override fun toString(): String {
        return "VersionInfo(id=$id, version=${version.value}, versionCode=${versionCode.value}, changes=${changes.value}," +
                " versionIconUrl=${versionIconUriHolderState.value}, versionBannerUrl=${versionBannerUriHolderState.value}, " +
                "packageSize=${packageSize.value}, privacyPolicyUrl=${privacyPolicyUrl.value}," +
                " screenshotInfos=$screenshotInfoForCreates, downloadInfos=$downloadInfoForCreates)"
    }

    fun addScreenshotForCreate(): ScreenshotInfoForCreate {
        val screenshotInfoForCreate = ScreenshotInfoForCreate()
        screenshotInfoForCreates.add(screenshotInfoForCreate)
        return screenshotInfoForCreate
    }

    fun addDownloadInfoForCreate() {
        val downloadInfoForCreate = DownloadInfoForCreate()
        downloadInfoForCreates.add(downloadInfoForCreate)
    }

    fun inflateFromVersionInfo(versionInfo: VersionInfo) {
        Log.e("VersionInfoForCreate", "inflateFromVersionInfo")
        versionInfo.versionIconUrl?.let {
            versionIconUriHolderState.value = object : UriHolder {
                override fun provideUri(): Uri? {
                    return Uri.parse(it)
                }

                override fun isLocalUri(): Boolean = false
            }
        }
        versionInfo.versionBannerUrl?.let {
            versionBannerUriHolderState.value = object : UriHolder {
                override fun provideUri(): Uri? {
                    return Uri.parse(it)
                }

                override fun isLocalUri(): Boolean = false
            }
        }
        versionInfo.id?.let {
            id = it
        }
        versionInfo.version?.let {
            version.value = it
        }
        versionInfo.versionCode?.let {
            versionCode.value = it.toString()
        }
        versionInfo.changes?.let {
            changes.value = it
        }
        versionInfo.packageSize?.let {
            packageSize.value = it
        }
        versionInfo.privacyUrl?.let {
            privacyPolicyUrl.value = it
        }
        versionInfo.screenshotInfos?.forEach {
            val screenshotInfoForCreate = ScreenshotInfoForCreate()
            screenshotInfoForCreate.inflateFromScreenshotInfo(it)
            screenshotInfoForCreates.add(screenshotInfoForCreate)
        }
        versionInfo.downloadInfos?.forEach {
            val downloadInfoForCreate = DownloadInfoForCreate()
            downloadInfoForCreate.inflateFromDownloadInfo(it)
            downloadInfoForCreates.add(downloadInfoForCreate)
        }
    }

}

/**
 * @param type video,image
 */
class ScreenshotInfoForCreate {
    val id: String? = null
    val uriHolderState: MutableState<UriHolder?> = mutableStateOf(null)
    var type: String? = null
    override fun toString(): String {
        return "ScreenshotInfo(id=$id, url=${uriHolderState.value}, type=$type)"
    }

    fun inflateFromScreenshotInfo(screenshotInfo: ScreenshotInfo) {
        Log.e("ScreenshotInfoForCreate", "inflateFromScreenshotInfo")
        screenshotInfo.url?.let {
            uriHolderState.value = object : UriHolder {
                override fun provideUri(): Uri? {
                    return Uri.parse(it)
                }

                override fun isLocalUri(): Boolean = false
            }
        }
    }
}

class DownloadInfoForCreate {
    var id: String? = null
    val url: MutableState<String> = mutableStateOf("")
    override fun toString(): String {
        return "DownloadInfo(id=$id, url=${url.value})"
    }

    fun inflateFromDownloadInfo(downloadInfo: DownloadInfo) {
        Log.e("DownloadInfoForCreate", "inflateFromDownloadInfo")
        downloadInfo.url?.let {
            url.value = "********"
        }
    }

}