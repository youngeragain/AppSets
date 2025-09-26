package xcj.app.appsets.ui.compose.quickstep

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable

sealed interface HandlerClickParams {

    data object SimpleClick : HandlerClickParams

    data class RequestReplaceHostContent(
        val replaceRequest: String,
        val payload: Any? = null
    ) : HandlerClickParams

}

abstract class QuickStepContentHandler {
    abstract val name: Int
    abstract val description: Int
    abstract val category: Int
    abstract fun canAccept(quickStepContentHolder: QuickStepContentHolder): Boolean

    abstract fun getContent(onClick: (HandlerClickParams) -> Unit): @Composable () -> Unit

    open fun getHostReplaceContent(requestReplaceHostContent: HandlerClickParams.RequestReplaceHostContent): @Composable () -> Unit {
        val contentCompose = @Composable {
            Box {}
        }
        return contentCompose
    }
}
