package xcj.app.appsets.ui.nonecompose.base

import android.net.Uri

interface UriHolder {
    fun provideUri(): Uri?
    fun isLocalUri(): Boolean = true
}