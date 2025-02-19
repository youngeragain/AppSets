package xcj.app.appsets.service

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.ui.platform.ComposeView
import xcj.app.appsets.ui.compose.ime.IMEMainContent

class AppSetsIMEService : InputMethodService() {
    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                IMEMainContent()
            }
        }
    }
}