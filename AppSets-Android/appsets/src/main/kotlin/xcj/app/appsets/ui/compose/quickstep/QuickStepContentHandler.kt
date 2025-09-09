package xcj.app.appsets.ui.compose.quickstep

import androidx.compose.runtime.Composable

interface QuickStepContentHandler {
    val name: Int
    val description: Int
    val category: Int
    fun accept(quickStepContentHolder: QuickStepContentHolder): Boolean
    fun getContent(onClick: () -> Unit): @Composable () -> Unit
}
