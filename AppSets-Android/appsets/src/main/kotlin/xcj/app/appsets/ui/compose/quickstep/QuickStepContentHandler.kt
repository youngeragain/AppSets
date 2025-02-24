package xcj.app.appsets.ui.compose.quickstep

import androidx.compose.runtime.Composable

interface QuickStepContentHandler {
    fun getName(): String
    fun getCategory(): String
    fun accept(contentTypes: List<String>): Boolean
    fun handleContent(content: Any)
    fun getContent(): @Composable () -> Unit
}
