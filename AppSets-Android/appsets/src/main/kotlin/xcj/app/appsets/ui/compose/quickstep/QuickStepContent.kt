package xcj.app.appsets.ui.compose.quickstep

import android.net.Uri
import xcj.app.starter.util.ContentType

interface QuickStepContent {
    fun getContentTypes(): List<String>
}

class TextQuickStepContent(
    val text: String
) : QuickStepContent {
    override fun getContentTypes(): List<String> {
        return listOf(ContentType.TEXT_PLAIN)
    }
}

class UriQuickStepContent(
    val uri: Uri,
) : QuickStepContent {
    override fun getContentTypes(): List<String> {
        return listOf(ContentType.TEXT_PLAIN)
    }
}
