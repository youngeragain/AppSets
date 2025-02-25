package xcj.app.appsets.ui.compose.quickstep

import androidx.compose.runtime.Composable

interface QuickStepContentHandler {
    fun getName(): String
    fun getCategory(): String
    fun accept(contents: List<QuickStepContent>): Boolean
    fun getContent(onClick: () -> Unit): @Composable () -> Unit
}
