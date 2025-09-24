package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.R
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.ui.model.TipsProvider

sealed interface GroupInfoPageState : TipsProvider {

    data class LoadSuccess(
        val groupInfo: GroupInfo,
        override val tipsIntRes: Int? = null,
        override val subTipsIntRes: Int? = null
    ) : GroupInfoPageState

    data object Loading : GroupInfoPageState {
        override val tipsIntRes: Int? = R.string.loading
        override val subTipsIntRes: Int? = null
    }

    data object NotFound : GroupInfoPageState {
        override val tipsIntRes: Int? = R.string.not_found
        override val subTipsIntRes: Int? = null
    }

}