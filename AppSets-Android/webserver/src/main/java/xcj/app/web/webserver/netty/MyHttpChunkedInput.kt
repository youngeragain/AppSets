package xcj.app.web.webserver.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.handler.codec.http.HttpChunkedInput
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.stream.ChunkedInput
import xcj.app.starter.android.util.PurpleLogger

class MyHttpChunkedInput(input: ChunkedInput<ByteBuf>, private val dataLength: Long? = null) :
    HttpChunkedInput(input) {
    companion object {
        private const val TAG = "MyHttpChunkedInput"
    }

    override fun readChunk(allocator: ByteBufAllocator?): HttpContent? {
        val readChunk = super.readChunk(allocator)
        return readChunk

    }

    override fun length(): Long {
        val superLength = super.length()
        val returnLength = dataLength ?: superLength
        PurpleLogger.current.d(TAG, "length, superLength:$superLength, returnLength:$returnLength")
        return superLength
    }

    override fun close() {
        val progress = progress()
        PurpleLogger.current.d(TAG, "close, progress:$progress")
        super.close()
    }
}