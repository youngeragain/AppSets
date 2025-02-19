package xcj.app.web.webserver.base

import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import java.io.Closeable

data class FileUploadN(val fileUpload: FileUpload) : Closeable {

    var amount: Int = 0
    var next: FileUploadN? = null

    var httpPostRequestDecoder: HttpPostRequestDecoder? = null

    override fun close() {
        httpPostRequestDecoder?.destroy()
        httpPostRequestDecoder = null
        if (next != null) {
            next?.close()
            next = null
        }
    }
}
