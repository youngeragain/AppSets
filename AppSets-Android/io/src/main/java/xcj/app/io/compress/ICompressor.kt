package xcj.app.io.compress

import android.content.Context
import java.io.File

interface ICompressor {
    interface CompressOptions {
        fun imageCompressQuality(): Int
    }

    suspend fun compress(context: Context, file: File, compressOption: CompressOptions?): File
}