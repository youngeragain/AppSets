package xcj.app.io.compress

import android.content.Context
import java.io.File

class CompressorHelper : ICompressor {

    private fun File.isImage(): Boolean {
        return when (extension.lowercase()) {
            "png", "webp", "jpeg", "bmp", "svg", "jpg", "tif", "tiff" -> true
            else -> false
        }
    }

    private fun File.isVideo(): Boolean {
        return when (extension.lowercase()) {
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "ts", "mpeg", "mpg", "m2ts", "ogv" -> true
            else -> false
        }
    }

    private fun File.isAudio(): Boolean {
        return when (extension.lowercase()) {
            "mp3", "aac", "wav", "flac", "ogg", "opus", "wma", "aiff", "m4a" -> true
            else -> false
        }
    }

    override suspend fun compress(
        context: Context,
        file: File,
        compressOption: ICompressor.CompressOptions?
    ): File {
        return getCompressor(file).compress(context, file, compressOption)
    }

    private fun getCompressor(file: File): ICompressor {
        return createCompressor(file)
    }

    private fun createCompressor(file: File): ICompressor {
        return when {
            file.isImage() -> {
                ImageCompressor()
            }

            file.isAudio() -> {
                AudioCompressor()
            }

            file.isVideo() -> {
                VideoCompressor()
            }

            else -> {
                NothingCompressor()
            }
        }
    }
}