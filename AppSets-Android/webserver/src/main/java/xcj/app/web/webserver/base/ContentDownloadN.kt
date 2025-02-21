package xcj.app.web.webserver.base

import java.io.Closeable

data class ContentDownloadN(
    val id: String,
    val name: String,
    val readableData: ReadableData,
    val progressListener: ProgressListener? = null
) : Closeable {
    override fun close() {

    }
}
