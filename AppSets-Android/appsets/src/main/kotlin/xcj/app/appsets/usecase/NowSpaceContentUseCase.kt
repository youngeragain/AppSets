package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.ui.model.NowSpaceObjectState
import xcj.app.compose_share.dynamic.IComposeDispose
import xcj.app.starter.android.util.PurpleLogger

class NowSpaceContentUseCase private constructor() : IComposeDispose {

    companion object {
        private const val TAG = "NowSpaceContentUseCase"
        private var INSTANCE: NowSpaceContentUseCase? = null

        fun getInstance(): NowSpaceContentUseCase {
            return INSTANCE ?: run {
                val useCase = NowSpaceContentUseCase()
                INSTANCE = useCase
                useCase
            }
        }
    }

    private val _content: MutableState<NowSpaceObjectState> =
        mutableStateOf(NowSpaceObjectState.NULL)

    val content: State<NowSpaceObjectState> = _content

    fun onNewImMessage(session: Session, imMessage: ImMessage) {
        PurpleLogger.current.d(TAG, "onNewImMessage")
        _content.value = NowSpaceObjectState.NewImMessage(session, imMessage)
    }

    fun removeContent() {
        PurpleLogger.current.d(TAG, "removeContent")
        _content.value = NowSpaceObjectState.NULL
    }

    fun removeContentIf(test: (NowSpaceObjectState) -> Boolean) {
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