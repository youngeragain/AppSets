package xcj.app.compose_share.components

import androidx.activity.BackEventCompat
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.FlowCollector

open class ProgressiveVisibilityComposeState : VisibilityComposeState(), FlowCollector<Any> {

    private val _progressState: MutableState<Any> = mutableStateOf(Unit)

    val progressState: State<Any> = _progressState

    val progress: Float
        get() {
            val value = progressState.value
            return if (value is BackEventCompat) {
                value.progress
            } else {
                0f
            }
        }

    var progressStated: Boolean = false
    var progressEnded: Boolean = false

    fun markStarted() {
        progressStated = true
        progressEnded = false
    }

    fun markEnded() {
        progressStated = true
        progressEnded = true
    }

    fun reset() {
        progressStated = false
        progressEnded = false
    }

    private fun onProgress(progressObject: Any) {
        if (progressObject is BackEventCompat) {
            if (progressObject.progress == 0f) {
                markStarted()
            }
        }
        _progressState.value = progressObject

        if (progressObject is BackEventCompat) {
            if (progressObject.progress == 1f) {
                markEnded()
            }
        }
    }

    override suspend fun emit(value: Any) {
        onProgress(value)
    }

    fun asBackEventState(): BackEventCompat {
        return progressState.value as BackEventCompat
    }
}