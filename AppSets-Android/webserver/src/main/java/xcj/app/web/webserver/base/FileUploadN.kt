package xcj.app.web.webserver.base

import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import java.io.Closeable

data class FileUploadN(val fileUploadList: List<FileUpload>) : Closeable {
    var httpPostRequestDecoder: HttpPostRequestDecoder? = null

    override fun close() {
        httpPostRequestDecoder?.destroy()
        httpPostRequestDecoder = null
    }
}
