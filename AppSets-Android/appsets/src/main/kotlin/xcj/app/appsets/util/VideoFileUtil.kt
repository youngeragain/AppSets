package xcj.app.appsets.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.util.ktx.writeBitmap
import xcj.app.appsets.util.model.UriProvider
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

object VideoFileUtil {

    private const val TAG = "VideoFileUtil"
    suspend fun getVideoFirstFrameAsUriProvider(context: Context, uri: Uri): UriProvider? {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        val thumbnailBitmap = getVideoFirstFrame(context, uri, mediaMetadataRetriever)
        if (thumbnailBitmap == null) {
            return null
        }
        val filePathDir =
            LocalAndroidContextFileDir.current.tempImagesCacheDir
        if (filePathDir.isNullOrEmpty()) {
            return null
        }
        val fileName = "${System.currentTimeMillis()}.png"
        val file = File(filePathDir, fileName)
        file.createNewFile()
        file.writeBitmap(thumbnailBitmap, Bitmap.CompressFormat.PNG, 65)
        return UriProvider.fromFile(file)
    }

    suspend fun getVideoFirstFrame(
        context: Context,
        uri: Uri,
        mediaMetadataRetriever: MediaMetadataRetriever
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            runCatching {
                PurpleLogger.current.d(TAG, "fillThumbnail before")
                mediaMetadataRetriever.setDataSource(context, uri)
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