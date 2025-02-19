package xcj.app.starter.android

import android.content.Context
import android.net.Uri

interface PurpleModulePageUri {
    fun requestContext(): Context
    fun getRequestUri(): Uri
}
