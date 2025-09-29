package xcj.app.appsets.ui.compose.outside

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

class RestrictedContentHandleState {


    private val _isShow: MutableState<Boolean> = mutableStateOf(false)
    val isShow: State<Boolean> = _isShow

    private var callback: (() -> Unit)? = null

    fun setCallback(callback: (() -> Unit)? = null) {
        this.callback = callback
    }

    fun doCallback() {
        callback?.invoke()
    }

    fun hide() {
        _isShow.value = false
    }

    fun show() {
        _isShow.value = true
    }
}

@Composable
fun rememberRestrictedContentHandleState(): RestrictedContentHandleState {
    val handle = remember {
        RestrictedContentHandleState()
    }
    return handle
}