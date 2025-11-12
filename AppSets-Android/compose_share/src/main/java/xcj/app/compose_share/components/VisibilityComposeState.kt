@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.compose_share.components

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import kotlinx.coroutines.flow.MutableStateFlow

abstract class VisibilityComposeState {
    val composableStateAvailableFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var shouldBackgroundSinkingDownwards: Boolean = false
    private val _showState: MutableState<Boolean> = mutableStateOf(false)
    private var composeHolder: ComposeViewProvider? = null

    val showState: State<Boolean> = _showState

    val showStateFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isShow
        get() = _showState.value

    private var hideCallback: (() -> Unit)? = null

    fun hide() {
        _showState.value = false
        showStateFlow.value = false
        hideCallback?.invoke()
        hideCallback = null
    }

    fun animateToHide() {
        _showState.value = false
        showStateFlow.value = false
    }

    fun hideAndRemove() {
        hide()
        composeHolder = null
    }

    fun show(hideCallback: (() -> Unit)? = null) {
        this.hideCallback = hideCallback
        _showState.value = true
        showStateFlow.value = true
    }

    fun show(hideCallback: (() -> Unit)? = null, composeHolder: ComposeViewProvider) {
        setContent(composeHolder)
        show(hideCallback)
    }

    fun show(hideCallback: (() -> Unit)? = null, content: @Composable () -> Unit) {
        val provider = object : ComposeViewProvider {
            override fun provideComposeView(context: Context): ComposeView {
                return ComposeView(context).apply {
                    setContent { content() }
                }
            }
        }
        show(hideCallback, provider)
    }

    fun setShouldBackgroundSink(sink: Boolean) {
        shouldBackgroundSinkingDownwards = sink
    }

    fun shouldBackgroundSink(): Boolean {
        return shouldBackgroundSinkingDownwards
    }

    fun setContent(composeHolder: ComposeViewProvider?) {
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
        if (isShow) {
            hide()
        } else {
            show()
        }
    }

    fun markComposeAvailableState(available: Boolean) {
        composableStateAvailableFlow.value = available
    }
}