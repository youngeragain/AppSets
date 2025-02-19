package xcj.app.appsets.ui.model

import xcj.app.appsets.server.model.GroupInfo

sealed class GroupInfoState(override val tips: Int? = null) : TipsState {

    data class LoadSuccess(val groupInfo: GroupInfo) :
        GroupInfoState(null)

    data object Loading : GroupInfoState(xcj.app.appsets.R.string.loading)

    data object NotFound : GroupInfoState(xcj.app.appsets.R.string.not_found)

}