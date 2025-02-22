package xcj.app.share.http.common

import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import xcj.app.share.base.DataContent
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.base.ProgressListener

class ProgressResponseBody(
    private val dataContent: DataContent,
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener?
) : ResponseBody() {
    companion object {
        private const val TAG = "ProgressResponseBody"
    }

    private var bufferedSource: BufferedSource? = null

    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            val contentLength = contentLength()

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                if (bytesRead > 0) {
                    totalBytesRead += bytesRead
                }
                val progressListener = progressListener
                if (progressListener != null) {
                    val dataProgressInfo = DataProgressInfoPool.obtainById(dataContent.id)
                    dataProgressInfo.total = contentLength
                    dataProgressInfo.name = dataContent.name
                    dataProgressInfo.current = totalBytesRead
                    progressListener.onProgress(dataProgressInfo)
                }
                return bytesRead
            }
        }
    }
}