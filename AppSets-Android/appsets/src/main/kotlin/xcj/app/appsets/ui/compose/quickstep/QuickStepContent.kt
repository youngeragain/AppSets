package xcj.app.appsets.ui.compose.quickstep

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import xcj.app.starter.android.util.AndroidUriFile
import xcj.app.starter.util.ContentType

interface QuickStepContent : Parcelable {
    fun getContentType(): String
}

@Parcelize
class TextQuickStepContent(
    val text: String
) : QuickStepContent {

    override fun getContentType(): String {
        return ContentType.TEXT_PLAIN
    }
}

@Parcelize
class UriQuickStepContent(
    val uri: Uri,
    val androidUriFile: AndroidUriFile?,
    val uriContentType: String
) : QuickStepContent {
    override fun getContentType(): String {
        return uriContentType
    }
}

data class QuickStepContentHolder(
    val intent: Intent,
    val quickStepContents: List<QuickStepContent>
)