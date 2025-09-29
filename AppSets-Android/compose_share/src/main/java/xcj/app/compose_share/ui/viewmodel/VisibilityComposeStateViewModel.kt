package xcj.app.compose_share.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import xcj.app.compose_share.components.BottomSheetVisibilityComposeState
import xcj.app.compose_share.components.ProgressiveVisibilityComposeState
import xcj.app.compose_share.components.VisibilityComposeState
import xcj.app.compose_share.components.VisibilityComposeStateProvider
import xcj.app.starter.android.util.PurpleLogger

abstract class VisibilityComposeStateViewModel : ViewModel(), VisibilityComposeStateProvider {

    companion object {
        private const val TAG = "VisibilityComposeStateViewModel"
        private const val NAME_BOTTOM_SHEET_COMPOSE_STATE = "bottomSheetContainerComposeState"
        private const val NAME_IMMERSE_CONTENT_COMPOSE_STATE = "immerseContentComposeState"

        fun VisibilityComposeStateProvider.bottomSheetState(): VisibilityComposeState {
            return provideState(NAME_BOTTOM_SHEET_COMPOSE_STATE) {
                BottomSheetVisibilityComposeState()
            }
        }

        fun VisibilityComposeStateProvider.immerseContentState(): VisibilityComposeState {
            return provideState(NAME_IMMERSE_CONTENT_COMPOSE_STATE) {
                ProgressiveVisibilityComposeState()
            }
        }
    }

    private val visibilityComposeStateMap: MutableMap<String, VisibilityComposeState> =
        mutableMapOf()

    override fun provideState(
        name: String,
        default: () -> VisibilityComposeState
    ): VisibilityComposeState {
        if (!visibilityComposeStateMap.containsKey(name)) {
            PurpleLogger.current.d(
                TAG,
                "provideState, name:$name no state can provide! new default provided!"
            )
        } else {
            PurpleLogger.current.d(
                TAG,
                "provideState, name:$name"
            )
        }
        val containerState = visibilityComposeStateMap.getOrPut(name, default)
        return containerState
    }

    open fun handleIntent(intent: Intent) {

    }
}