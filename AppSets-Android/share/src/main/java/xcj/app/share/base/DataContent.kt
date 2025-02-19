package xcj.app.share.base

import android.net.Uri
import xcj.app.starter.android.util.AndroidUriFile
import java.io.Closeable
import java.io.File
import java.util.UUID

sealed interface DataContent {

    val id: String
    var clientInfo: ClientInfo

    data class UriContent(
        val uri: Uri,
        val androidUriFile: AndroidUriFile? = null,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent

    //out is FileOutputStream or FileChannel
    data class FileContent(
        val file: File,
        var out: Closeable? = null,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent

    data class StringContent(
        val content: String,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent

    data class ByteArrayContent(
        val bytes: ByteArray,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent
}