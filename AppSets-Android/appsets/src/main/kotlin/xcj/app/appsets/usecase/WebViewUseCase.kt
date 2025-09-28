package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.compose_share.dynamic.ComposeLifecycleAware

class WebViewUseCase : ComposeLifecycleAware {

    val url: MutableState<String?> = mutableStateOf(null)

    override fun onComposeDispose(by: String?) {

    }
}