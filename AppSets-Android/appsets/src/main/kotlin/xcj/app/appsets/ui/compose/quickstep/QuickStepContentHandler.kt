package xcj.app.appsets.ui.compose.quickstep

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

sealed interface HandlerClickParams {

    data object SimpleClick : HandlerClickParams

    data class ReplaceHostContentRequest(
        val request: String,
        val payload: Any? = null
    ) : HandlerClickParams

}

data class QuickStepInfo(
    val name: Int,
    val description: Int,
    val category: Int
)

abstract class QuickStepContentHandler {

    abstract val quickStepInfo: QuickStepInfo

    abstract fun canAccept(quickStepContentHolder: QuickStepContentHolder): Boolean

    open fun onSearch(context: Context, content: String): Boolean {
        if (content.isEmpty()) {
            return true
        }
        val name = ContextCompat.getString(context, quickStepInfo.name)
        if (name.contains(content)) {
            return true
        }
        val category = ContextCompat.getString(context, quickStepInfo.category)
        return category.contains(content)
    }

    abstract fun getContent(onClick: (HandlerClickParams) -> Unit): @Composable () -> Unit

    open fun getHostReplaceContent(
        replaceHostContentRequest: HandlerClickParams.ReplaceHostContentRequest
    ): @Composable () -> Unit {
        val replaceContentCompose = @Composable {
            Box {}
        }
        return replaceContentCompose
    }
}
