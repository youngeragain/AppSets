package xcj.app.appsets.util.model

import android.net.Uri

interface UriProvider {
    fun provideUri(): Uri?
    fun isLocalUri(): Boolean = true
}

fun String?.parseHttpUriHolder(): UriProvider? {
    if (this.isNullOrEmpty()) {
        return null
    }
    return object : UriProvider {

        override fun provideUri(): Uri? {
            return Uri.parse(this@parseHttpUriHolder)
        }

        override fun isLocalUri(): Boolean = false
    }
}