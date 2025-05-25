package xcj.app.appsets.util.model

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger

data class MediaStoreDataUri(
    var id: String? = null,
    var uri: Uri? = null,
    var date: String? = null,
    var displayName: String? = null,
    var size: Long = 0L,
    var sizeReadable: String? = null
) : UriProvider {
    companion object {
        private const val TAG = "MediaStoreDataUriWrapper"
        fun fromUri(uri: Uri): MediaStoreDataUri {
            return MediaStoreDataUri().apply {
                this.uri = uri
            }
        }
    }

    override fun provideUri(): Uri? {
        return uri
    }

    suspend fun getThumbnail(
        context: Context,
        mediaMetadataRetriever: MediaMetadataRetriever
    ): Bitmap? {
        val provideUri = provideUri()
        if (provideUri == null) {
            return null
        }
        return withContext(Dispatchers.IO) {
            runCatching {
                PurpleLogger.current.d(TAG, "fillThumbnail before")
                mediaMetadataRetriever.setDataSource(context, provideUri)
                val thumbnailsBitmap = mediaMetadataRetriever.frameAtTime
                mediaMetadataRetriever.release()
                PurpleLogger.current.d(TAG, "fillThumbnail after")
                return@withContext thumbnailsBitmap
            }.onFailure {
                PurpleLogger.current.d(TAG, "fillThumbnail failed!")
            }
            return@withContext null
        }

    }
}