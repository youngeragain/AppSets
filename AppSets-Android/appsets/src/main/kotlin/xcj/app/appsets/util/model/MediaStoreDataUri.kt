package xcj.app.appsets.util.model

import android.net.Uri
import xcj.app.starter.android.util.UriProvider
import xcj.app.starter.util.ContentType

data class MediaStoreDataUri(
    val id: Long,
    val uri: Uri,
    val date: String? = null,
    val displayName: String? = null,
    val size: Long = 0L,
    val sizeReadable: String? = null,
    val mimeType: String? = null
) : UriProvider {

    override fun provideUri(): Uri {
        return uri
    }
}

fun UriProvider.isImageType(): Boolean {
    return this is MediaStoreDataUri && ContentType.isImage(this.mimeType)
}

fun UriProvider.isVideoType(): Boolean {
    return this is MediaStoreDataUri && ContentType.isVideo(this.mimeType)
}

fun UriProvider.isAudioType(): Boolean {
    return this is MediaStoreDataUri && ContentType.isAudio(this.mimeType)
}