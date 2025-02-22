package xcj.app.web.webserver.netty

import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.FileUploadN
import java.io.Closeable

class HttpRequestWrapper(
    val httpRequest: HttpRequest,
    val queryStringDecoder: QueryStringDecoder,
    val port: Int
) : Closeable {
    companion object {
        private const val TAG = "HttpRequestWrapper"
    }

    var httpPostRequestDecoder: HttpPostRequestDecoder? = null
    var fileUploadN: FileUploadN? = null

    var httpContent: HttpContent? = null
    var lastHttpContent: LastHttpContent? = null

    override fun close() {
        PurpleLogger.current.d(TAG, "close")
        httpPostRequestDecoder?.destroy()
        httpPostRequestDecoder = null
        httpContent = null
        lastHttpContent = null
        fileUploadN?.close()
    }
}