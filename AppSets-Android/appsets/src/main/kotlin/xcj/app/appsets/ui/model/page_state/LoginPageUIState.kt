package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider

sealed interface LoginPageUIState : TipsProvider {

    data class LoginStart(
        override val tips: Int? = null,
        override val subTips: Int? = null,
    ) : LoginPageUIState

    data class Logging(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginPageUIState

    data class LoggingSuccess(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginPageUIState

    data class LoggingFailed(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginPageUIState
}

