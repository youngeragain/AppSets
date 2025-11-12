package xcj.app.starter.android.util

import android.net.Uri
import androidx.core.net.toUri
import xcj.app.starter.foundation.Provider
import java.io.File

interface UriProvider : Provider<Uri> {
    fun provideUri(): Uri

    override fun provide(): Uri {
        return provideUri()
    }

    fun isLocalUri(): Boolean = true

    companion object {
        fun fromUri(uri: Uri): UriProvider {
            return object : UriProvider {
                override fun provideUri(): Uri {
                    return uri
                }
            }
        }

        fun fromFile(file: File): UriProvider? {
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