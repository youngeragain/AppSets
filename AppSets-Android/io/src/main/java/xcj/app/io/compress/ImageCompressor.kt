package xcj.app.io.compress

import android.content.Context
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

class ImageCompressor : NothingCompressor() {
    companion object {
        private const val TAG = "ImageCompressor"
    }

    override suspend fun compress(
        context: Context,
        file: File,
        compressOption: ICompressor.CompressOptions?
    ): File {
        val compressedFile = Compressor.compress(context, file) {
            default(quality = compressOption?.imageCompressQuality() ?: 80)
            val tempFilesCacheDir = LocalAndroidContextFileDir.current.tempFilesCacheDir
            val cacheFile =
                File(tempFilesCacheDir + File.separator + System.currentTimeMillis() + "." + file.extension.lowercase())
            destination(cacheFile)
        }
        if (compressedFile.exists()) {
            PurpleLogger.current.d(
                TAG,
                "compress, compressed image file:${compressedFile}"
            )
            return compressedFile
        }
        return file
    }
}