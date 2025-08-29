package xcj.app.io.compress

import android.content.Context
import xcj.app.starter.android.util.FileUtil.isAudio
import xcj.app.starter.android.util.FileUtil.isImage
import xcj.app.starter.android.util.FileUtil.isVideo
import java.io.File

class CompressorHelper : ICompressor {

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