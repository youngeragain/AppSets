package xcj.app.appsets.ui.model.state

import xcj.app.appsets.server.model.UpdateCheckResult

sealed interface AppUpdateState {

    data object None : AppUpdateState

    data object Checking : AppUpdateState

    data class Checked(
        val updateCheckResult: UpdateCheckResult
    ) : AppUpdateState
}