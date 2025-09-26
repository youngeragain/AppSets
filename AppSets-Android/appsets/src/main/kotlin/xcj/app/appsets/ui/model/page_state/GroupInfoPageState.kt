package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.ui.model.TipsProvider

sealed interface GroupInfoPageState : TipsProvider {

    data class LoadSuccess(
        val groupInfo: GroupInfo,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : GroupInfoPageState

    data object Loading : GroupInfoPageState {
        override val tips: Int? = xcj.app.appsets.R.string.loading
        override val subTips: Int? = null
    }

    data object NotFound : GroupInfoPageState {
        override val tips: Int? = xcj.app.appsets.R.string.not_found
        override val subTips: Int? = null
    }

}