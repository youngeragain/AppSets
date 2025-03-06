package xcj.app.share.base

import android.net.Uri
import xcj.app.starter.android.util.AndroidUriFile
import java.io.Closeable
import java.io.File
import java.util.UUID

sealed interface DataContent {

    val id: String
    val name: String
    var clientInfo: ClientInfo

    data class UriContent(
        val uri: Uri,
        val androidUriFile: AndroidUriFile? = null,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString()
    ) : DataContent {
        override val name: String = androidUriFile?.displayName ?: ""
    }

    //out is FileOutputStream or FileChannel
    data class FileContent(
        val file: File,
        var out: Closeable? = null,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent {
        override val name: String = file.name
    }

    data class StringContent(
        val content: String,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent {
        override val name: String = content
    }

    data class ByteArrayContent(
        val bytes: ByteArray,
        override var clientInfo: ClientInfo = ClientInfo.NONE_RESOLVED,
        override val id: String = UUID.randomUUID().toString(),
    ) : DataContent {
        override val name: String = if (bytes.size < 1024) {
            bytes.decodeToString()
        } else {
            ""
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ByteArrayContent

            if (!bytes.contentEquals(other.bytes)) return false
            if (clientInfo != other.clientInfo) return false
            if (id != other.id) return false
            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + clientInfo.hashCode()
            result = 31 * result + id.hashCode()
            result = 31 * result + name.hashCode()
            return result
        }
    }
}