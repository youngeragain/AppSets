package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider

sealed interface CreateScreenPageUIState : TipsProvider {

    data class CreateStart(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateScreenPageUIState

    data class Posting(
        override val tips: Int = xcj.app.appsets.R.string.adding,
        override val subTips: Int? = null
    ) : CreateScreenPageUIState

    data class CreateSuccess(
        override val tips: Int = xcj.app.appsets.R.string.create_success,
        override val subTips: Int? = null
    ) : CreateScreenPageUIState

    data class CreateFailed(
        override val tips: Int = xcj.app.appsets.R.string.create_failed,
        override val subTips: Int? = null
    ) : CreateScreenPageUIState
}