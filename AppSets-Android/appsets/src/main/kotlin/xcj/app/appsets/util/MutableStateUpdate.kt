package xcj.app.appsets.util

import androidx.compose.runtime.MutableState
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "MutableStateUpdate"

internal inline fun <T> MutableState<T>.updateState(crossinline update: T.() -> T?) {
    value.update()?.let {
        PurpleLogger.current.d(TAG, "updateState, it:${it}")
        value = it
    }
}