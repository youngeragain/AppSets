package xcj.app.appsets.util.model

import android.net.Uri
import androidx.core.net.toUri
import java.io.File

interface UriProvider {
    fun provideUri(): Uri

    fun isLocalUri(): Boolean = true

    companion object {
        fun fromUri(uri: Uri?): UriProvider? {
            if (uri == null) {
                return null
            }
            return object : UriProvider {
                override fun provideUri(): Uri {
                    return uri
                }
            }
        }

        fun fromFile(file: File?): UriProvider? {
            if (file == null) {
                return null
            }
            if (!file.exists()) {
                return null
            }
            val uri = file.toUri()
            return fromUri(uri)
        }

        fun fromString(str: String?): UriProvider? {
            if (str.isNullOrEmpty()) {
                return null
            }
            val uri = str.toUri()
            return fromUri(uri)
        }
    }
}