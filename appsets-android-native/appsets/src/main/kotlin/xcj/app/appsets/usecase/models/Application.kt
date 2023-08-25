package xcj.app.appsets.usecase.models

import android.os.Parcel
import android.os.Parcelable

data class Application(
    var iconUrl: String?,
    val website: String?,
    val updateTime: String?,
    val createTime: String?,
    val developerInfo: String?,
    var bannerUrl: String?,
    val createUid: String?,
    val updateUid: String?,
    val name: String?,
    val category: String?,
    val appId: String?,
    val platforms: List<PlatForm>?
) : Parcelable {

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
        parcel.createTypedArrayList(PlatForm)
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

    companion object CREATOR : Parcelable.Creator<Application> {
        override fun createFromParcel(parcel: Parcel): Application {
            return Application(parcel)
        }

        override fun newArray(size: Int): Array<Application?> {
            return arrayOfNulls(size)
        }
    }
}

data class PlatForm(
    val id: String?,
    val name: String?,
    val packageName: String?,
    val introduction: String?,
    val versionInfos: List<VersionInfo>?
) : Parcelable {
    var currentVisibleVersionInfoPosition: Int = -1
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

    companion object CREATOR : Parcelable.Creator<PlatForm> {
        override fun createFromParcel(parcel: Parcel): PlatForm {
            return PlatForm(parcel)
        }

        override fun newArray(size: Int): Array<PlatForm?> {
            return arrayOfNulls(size)
        }
    }
}

data class VersionInfo(
    val id: String?,
    var versionIconUrl: String?,
    var versionBannerUrl: String?,
    val version: String?,
    val versionCode: String?,
    val changes: String?,
    val packageSize: String?,
    val privacyUrl: String?,
    val screenshotInfos: List<ScreenshotInfo>?,
    val downloadInfos: List<DownloadInfo>?
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
    val id: String?,
    val createUid: String?,
    val updateUid: String?,
    val createTime: String?,
    val updateTime: String?,
    val type: String?,
    val contentType: String?,
    var url: String?,
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
    val id: String?,
    val createUid: String?,
    val updateUid: String?,
    val createTime: String?,
    val updateTime: String?,
    val downloadTimes: Int?,
    val url: String?,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
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
