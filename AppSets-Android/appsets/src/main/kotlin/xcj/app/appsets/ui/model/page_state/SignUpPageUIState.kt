package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider

sealed interface SignUpPageUIState : TipsProvider {

    data class SignUpStart(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SignUpPageUIState

    data class SignUpping(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SignUpPageUIState

    data class SignUpSuccess(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SignUpPageUIState

    class SignUpPageFailed(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SignUpPageUIState
}