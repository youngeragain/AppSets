package xcj.app.share.wlanp2p.base

import android.content.Context
import android.net.Uri
import java.io.Closeable
import java.io.File

interface WriteMethod : Closeable {

    fun writeChunkStartBytes(
        uuid: String,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    )

    fun writeChunkContentBytes(
        uuid: String,
        contentType: String,
        name: String?,
        totalLength: Long,
        writtenLength: Long,
        contentChunkBytes: ByteArray,
        startIndex: Int,
        length: Int
    )

    suspend fun writeUriContent(context: Context, uri: Uri)

    suspend fun writeFileContent(file: File)

    fun writeBytesContent(
        byteArray: ByteArray,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    )
}