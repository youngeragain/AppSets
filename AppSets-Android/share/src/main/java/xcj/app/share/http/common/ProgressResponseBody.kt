package xcj.app.share.http.common

import io.netty.handler.codec.http.HttpHeaderNames
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import xcj.app.share.base.DataContent
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.interfaces.ProgressListener

class ProgressResponseBody(
    private val dataContent: DataContent,
    private val originalResponse: Response,
    private val progressListener: ProgressListener?
) : ResponseBody() {
    companion object {
        private const val TAG = "ProgressResponseBody"
    }

    private val responseBody = originalResponse.body

    private val contentLengthInHeader =
        originalResponse.headers[HttpHeaderNames.CONTENT_LENGTH.toString()]?.toLongOrNull()

    private var bufferedSource: BufferedSource? = null

    override fun contentLength(): Long {
        if (contentLengthInHeader != null) {
            return contentLengthInHeader
        }
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
            var contentLength = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                if (contentLength == 0L) {
                    contentLength = contentLength()
                }
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