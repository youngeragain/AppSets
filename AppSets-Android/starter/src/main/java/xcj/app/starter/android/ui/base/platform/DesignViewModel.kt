package xcj.app.starter.android.ui.base.platform

import android.content.Intent
import androidx.lifecycle.ViewModel

abstract class DesignViewModel : ViewModel() {
    open fun handleIntent(intent: Intent) {}
}