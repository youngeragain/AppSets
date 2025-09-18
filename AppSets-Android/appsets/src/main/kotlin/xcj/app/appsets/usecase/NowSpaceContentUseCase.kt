package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.ui.model.state.NowSpaceObject
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger

class NowSpaceContentUseCase() : IComposeLifecycleAware {

    companion object {
        private const val TAG = "NowSpaceContentUseCase"
    }

    private val _content: MutableState<NowSpaceObject> =
        mutableStateOf(NowSpaceObject.NULL)

    val content: State<NowSpaceObject> = _content

    fun onNewImMessage(session: Session, imMessage: ImMessage) {
        PurpleLogger.current.d(TAG, "onNewImMessage")
        _content.value = NowSpaceObject.NewImMessage(session, imMessage)
    }

    fun removeContent() {
        PurpleLogger.current.d(TAG, "removeContent")
        _content.value = NowSpaceObject.NULL
    }

    fun removeContentIf(test: (NowSpaceObject) -> Boolean) {
        PurpleLogger.current.d(TAG, "removeContentIf")
        if (test(content.value)) {
            removeContent()
        }
    }

    override fun onComposeDispose(by: String?) {

    }

    fun onUserLogout() {
        removeContent()
    }
}