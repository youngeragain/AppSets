package xcj.app.compose_share.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import xcj.app.compose_share.components.AnyStateProvider
import xcj.app.compose_share.components.ComposeContainerState
import xcj.app.compose_share.components.ProgressedComposeContainerState
import xcj.app.starter.android.util.PurpleLogger

abstract class AnyStateViewModel : ViewModel(), AnyStateProvider {

    companion object {
        private const val TAG = "AnyStateViewModel"
        const val NAME_BOTTOM_SHEET_COMPOSE_STATE = "bottomSheetContainerComposeState"
        const val NAME_IMMERSE_CONTENT_COMPOSE_STATE = "immerseContentComposeState"
        fun AnyStateProvider.bottomSheetState(): ComposeContainerState {
            return provideState(NAME_BOTTOM_SHEET_COMPOSE_STATE)
        }

        fun AnyStateProvider.immerseContentState(): ComposeContainerState {
            return provideState(NAME_IMMERSE_CONTENT_COMPOSE_STATE)
        }
    }

    private val composeContainerStateMap: MutableMap<String, ComposeContainerState> = mutableMapOf()

    override fun provideState(name: String): ComposeContainerState {
        if (!composeContainerStateMap.containsKey(name)) {
            PurpleLogger.current.d(
                TAG,
                "provideState, name:$name no state can provide!, new default provided!"
            )
        } else {
            PurpleLogger.current.d(
                TAG,
                "provideState, name:$name"
            )
        }
        val containerState = composeContainerStateMap.getOrPut(name) {
            ProgressedComposeContainerState()
        }
        return containerState
    }

    open fun handleIntent(intent: Intent) {

    }
}