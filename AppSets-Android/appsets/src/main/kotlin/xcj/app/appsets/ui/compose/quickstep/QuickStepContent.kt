package xcj.app.appsets.ui.compose.quickstep

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import xcj.app.starter.android.util.AndroidUriFile
import xcj.app.starter.util.ContentType

interface QuickStepContent : Parcelable {
    fun getContentTypes(): List<String>
}

class TextQuickStepContent(
    val text: String
) : QuickStepContent {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "") {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(text)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun getContentTypes(): List<String> {
        return listOf(ContentType.TEXT_PLAIN)
    }

    companion object CREATOR : Parcelable.Creator<TextQuickStepContent> {

        override fun createFromParcel(parcel: Parcel): TextQuickStepContent {
            return TextQuickStepContent(parcel)
        }

        override fun newArray(size: Int): Array<TextQuickStepContent?> {
            return arrayOfNulls(size)
        }
    }
}

class UriQuickStepContent(
    val uri: Uri,
    val androidUriFile: AndroidUriFile?,
    val uriContentType: String
) : QuickStepContent {
    override fun getContentTypes(): List<String> {
        return listOf(uriContentType)
    }

    constructor(parcel: Parcel) : this(
        parcel.readParcelable<Uri>(UriQuickStepContent::class.java.classLoader) as Uri,
        null,
        parcel.readString() ?: ""
    ) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, 0)
        parcel.writeValue(null)
        parcel.writeString(uriContentType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UriQuickStepContent> {

        override fun createFromParcel(parcel: Parcel): UriQuickStepContent {
            return UriQuickStepContent(parcel)
        }

        override fun newArray(size: Int): Array<UriQuickStepContent?> {
            return arrayOfNulls(size)
        }
    }
}