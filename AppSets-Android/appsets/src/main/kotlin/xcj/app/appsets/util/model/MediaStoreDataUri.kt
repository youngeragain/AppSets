package xcj.app.appsets.util.model

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger

data class MediaStoreDataUri(
    val id: Long? = null,
    val uri: Uri? = null,
    val date: String? = null,
    val displayName: String? = null,
    val size: Long = 0L,
    val sizeReadable: String? = null,
    val mimeType: String? = null
) : UriProvider {
    companion object {
        private const val TAG = "MediaStoreDataUri"
        fun fromUri(uri: Uri): MediaStoreDataUri {
            return MediaStoreDataUri(
                uri = uri
            )
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