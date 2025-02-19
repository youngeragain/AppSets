package xcj.app.appsets.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import xcj.app.appsets.util.ktx.writeBitmap
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

object VideoFileUtil {
    suspend fun extractVideoFrame(context: Context, uriProvider: MediaStoreDataUri): Uri? {
        val mediaMetadataRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
        val thumbnailBitmap = uriProvider.getThumbnail(context, mediaMetadataRetriever)
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
        return file.toUri()
    }
}