package xcj.app.appsets.util.model

import android.net.Uri

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