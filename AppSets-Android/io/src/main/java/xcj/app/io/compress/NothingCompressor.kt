package xcj.app.io.compress

import android.content.Context
import java.io.File

open class NothingCompressor : ICompressor {
    override suspend fun compress(
        context: Context,
        file: File,
        compressOption: ICompressor.CompressOptions?
    ): File {
        return file
    }
}