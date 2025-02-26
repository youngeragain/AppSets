package xcj.app.appsets.ui.compose.quickstep

import android.content.Context
import androidx.compose.runtime.Composable

abstract class QuickStepContentHandler(val context: Context) {
    abstract fun getName(): String
    abstract fun getCategory(): String
    abstract fun accept(contents: List<QuickStepContent>): Boolean
    abstract fun getContent(onClick: () -> Unit): @Composable () -> Unit
}
