package xcj.app.compose_share.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.flow.FlowCollector

interface ComposeViewProvider {
    fun provideComposeView(context: Context): ComposeView
}

interface AnyStateProvider {
    /**
     * @param name, or key for ComposeContainerState
     */
    fun provideState(name: String): ComposeContainerState
}

open class ComposeContainerState {
    private var _showState: MutableState<Boolean> = mutableStateOf(false)
    private var composeHolder: ComposeViewProvider? = null

    val showState: State<Boolean> = _showState

    val isShow
        get() = _showState.value

    fun hide() {
        _showState.value = false
    }

    fun hideAndRemove() {
        hide()
        composeHolder = null
    }

    fun show() {
        _showState.value = true
    }

    fun show(composeHolder: ComposeViewProvider) {
        setContent(composeHolder)
        show()
    }

    fun show(content: @Composable () -> Unit) {
        val provider = object : ComposeViewProvider {
            override fun provideComposeView(context: Context): ComposeView {
                return ComposeView(context).apply {
                    setContent { content() }
                }
            }
        }
        show(provider)
    }

    fun setContent(composeHolder: ComposeViewProvider) {
        this.composeHolder = composeHolder
    }

    fun setContent(content: @Composable () -> Unit) {
        val provider = object : ComposeViewProvider {
            override fun provideComposeView(context: Context): ComposeView {
                return ComposeView(context).apply {
                    setContent { content() }
                }
            }
        }
        setContent(provider)
    }

    fun removeContent() {
        this.composeHolder = null
    }

    fun getContent(context: Context): ComposeView? {
        return composeHolder?.provideComposeView(context)
    }

    fun toggle() {
        _showState.value = !_showState.value
    }
}

class ProgressedComposeContainerState : ComposeContainerState(), FlowCollector<Any> {

    private val _progressState: MutableState<Any> = mutableStateOf(Unit)

    val progressState: State<Any> = _progressState

    var progressStated: Boolean = false

    fun markStarted() {
        progressStated = true
    }

    fun markEnded() {
        progressStated = false
    }

    suspend fun onProgress(progressObject: Any) {
        _progressState.value = progressObject
    }

    override suspend fun emit(value: Any) {
        onProgress(value)
    }
}