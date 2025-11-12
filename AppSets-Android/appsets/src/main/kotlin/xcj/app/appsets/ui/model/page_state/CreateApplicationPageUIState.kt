package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider

sealed interface CreateApplicationPageUIState : TipsProvider {

    data class CreateStart(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateApplicationPageUIState

    data class Creating(
        override val tips: Int = xcj.app.appsets.R.string.creating,
        override val subTips: Int? = null
    ) : CreateApplicationPageUIState

    data class CreateSuccess(
        override val tips: Int = xcj.app.appsets.R.string.create_application_success,
        override val subTips: Int? = null
    ) : CreateApplicationPageUIState

    data class CreateFailed(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateApplicationPageUIState

}