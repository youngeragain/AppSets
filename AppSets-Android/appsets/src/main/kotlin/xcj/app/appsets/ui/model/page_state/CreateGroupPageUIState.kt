package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider

sealed interface CreateGroupPageUIState : TipsProvider {
    data class CreateStart(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateGroupPageUIState

    data class Creating(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateGroupPageUIState

    data class CreateFailed(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateGroupPageUIState

    data class CreateSuccess(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateGroupPageUIState
}