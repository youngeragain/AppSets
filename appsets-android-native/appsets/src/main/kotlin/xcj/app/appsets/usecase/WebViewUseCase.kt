package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

class WebViewUseCase {
    val url: MutableState<String?> = mutableStateOf(null)
}