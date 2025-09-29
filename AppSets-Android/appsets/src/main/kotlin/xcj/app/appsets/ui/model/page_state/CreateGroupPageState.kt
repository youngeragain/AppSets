package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.GroupInfoForCreate

sealed interface CreateGroupPageState {
    val groupInfoForCreate: GroupInfoForCreate

    data class NewGroupPage(
        override val groupInfoForCreate: GroupInfoForCreate = GroupInfoForCreate()
    ) : CreateGroupPageState

    data class Creating(
        override val groupInfoForCreate: GroupInfoForCreate
    ) : CreateGroupPageState

    data class CreateFailedPage(
        override val groupInfoForCreate: GroupInfoForCreate
    ) : CreateGroupPageState

    data class CreateFinishPage(
        override val groupInfoForCreate: GroupInfoForCreate
    ) : CreateGroupPageState
}