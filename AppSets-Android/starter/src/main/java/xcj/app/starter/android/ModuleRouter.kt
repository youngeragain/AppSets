package xcj.app.starter.android

import android.content.Context
import android.content.Intent

interface ModuleRouter {

    fun findSupportPageUri(uri: PurpleModulePageUri): Intent?

    fun <T : Context> onPageRequest(
        uri: PurpleModulePageUri
    ) {
        findSupportPageUri(uri)?.let {
            val requestContext = uri.requestContext()
            runCatching {
                requestContext.startActivity(it)
            }
        }
    }
}