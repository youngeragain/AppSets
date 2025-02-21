package xcj.app.share.http.common

import xcj.app.web.webserver.base.InputStreamReadableData
import java.io.InputStream

data class DataContentReadableData(
    private val dataLength: Long,
    private val dataInputStream: InputStream
) : InputStreamReadableData {

    override fun getLength(): Long {
        return dataLength
    }


    override fun getInputStream(): InputStream {
        return dataInputStream
    }
}