package xcj.app.appsets.server.model

import android.os.Parcel
import android.os.Parcelable
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.ImSessionHolder
import xcj.app.appsets.im.Session
import java.util.UUID

data class Application(
    val appId: String? = null,
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
    override val name: String? = null,
    val category: String? = null,
    val platforms: List<AppPlatform>? = null,
) : Bio, ImSessionHolder, Parcelable {

    override var imSession: Session? = null

    override val id: String
        get() = "$BIO_ID_PREFIX${appId ?: UUID.randomUUID().toString()}"

    override var bioUrl: Any? = null

    var currentVisiblePlatformPosition: Int = -1

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(AppPlatform)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(iconUrl)
        parcel.writeString(website)
        parcel.writeString(updateTime)
        parcel.writeString(createTime)
        parcel.writeString(developerInfo)
        parcel.writeString(bannerUrl)
        parcel.writeString(createUid)
        parcel.writeString(updateUid)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(appId)
        parcel.writeTypedList(platforms)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun platformVersionDownloadsInfos(platformName: String?): List<DownloadInfo> {
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


    companion object CREATOR : Parcelable.Creator<Application> {

        const val BIO_ID_PREFIX = "APP-"

        override fun createFromParcel(parcel: Parcel): Application {
            return Application(parcel)
        }

        override fun newArray(size: Int): Array<Application?> {
            return arrayOfNulls(size)
        }

        fun basic(id: String, name: String?, iconUrl: String?): Application {
            return Application(appId = id, name = name, iconUrl = iconUrl).apply {
                bioUrl = iconUrl
            }
        }
    }
}

data class AppPlatform(
    val id: String? = null,
    val name: String? = null,
    val packageName: String? = null,
    val introduction: String? = null,
    val versionInfos: List<VersionInfo>? = null,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(VersionInfo)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(packageName)
        parcel.writeString(introduction)
        parcel.writeTypedList(versionInfos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppPlatform> {
        override fun createFromParcel(parcel: Parcel): AppPlatform {
            return AppPlatform(parcel)
        }

        override fun newArray(size: Int): Array<AppPlatform?> {
            return arrayOfNulls(size)
        }
    }
}

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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(ScreenshotInfo),
        parcel.createTypedArrayList(DownloadInfo)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(versionIconUrl)
        parcel.writeString(versionBannerUrl)
        parcel.writeString(version)
        parcel.writeValue(versionCode)
        parcel.writeString(changes)
        parcel.writeString(packageSize)
        parcel.writeString(privacyUrl)
        parcel.writeTypedList(screenshotInfos)
        parcel.writeTypedList(downloadInfos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VersionInfo> {
        override fun createFromParcel(parcel: Parcel): VersionInfo {
            return VersionInfo(parcel)
        }

        override fun newArray(size: Int): Array<VersionInfo?> {
            return arrayOfNulls(size)
        }
    }
}

data class ScreenshotInfo(
    val id: String? = null,
    val createUid: String? = null,
    val updateUid: String? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val type: String? = null,
    val contentType: String? = null,
    var url: String? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(createUid)
        parcel.writeString(updateUid)
        parcel.writeString(createTime)
        parcel.writeString(updateTime)
        parcel.writeString(type)
        parcel.writeString(contentType)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScreenshotInfo> {
        override fun createFromParcel(parcel: Parcel): ScreenshotInfo {
            return ScreenshotInfo(parcel)
        }

        override fun newArray(size: Int): Array<ScreenshotInfo?> {
            return arrayOfNulls(size)
        }
    }
}

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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(createUid)
        parcel.writeString(updateUid)
        parcel.writeString(createTime)
        parcel.writeString(updateTime)
        parcel.writeValue(downloadTimes)
        parcel.writeString(url)
        parcel.writeString(description)
        parcel.writeStringList(architectures)
        parcel.writeString(size)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DownloadInfo> {
        override fun createFromParcel(parcel: Parcel): DownloadInfo {
            return DownloadInfo(parcel)
        }

        override fun newArray(size: Int): Array<DownloadInfo?> {
            return arrayOfNulls(size)
        }
    }
}
