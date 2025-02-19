package xcj.app.appsets.ui.model

import xcj.app.appsets.server.model.ScreenInfo

sealed class ScreenState {

    data class Screen(val screenInfo: ScreenInfo) : ScreenState()

    data object NoMore : ScreenState()

}