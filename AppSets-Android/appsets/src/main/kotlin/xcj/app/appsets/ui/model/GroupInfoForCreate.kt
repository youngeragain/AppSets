package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import xcj.app.appsets.ui.model.page_state.CreateGroupPageState
import xcj.app.appsets.util.model.UriProvider

data class GroupInfoForCreate(
    val name: String = "",
    val membersCount: String = "",
    val isPublic: Boolean = false,
    val introduction: String = "",
    val icon: UriProvider? = null
) {
    companion object {
        fun updateGroupCreateIconUri(
            state: MutableState<CreateGroupPageState>,
            uriProvider: UriProvider?
        ) {
            val oldState = state.value as? CreateGroupPageState.NewGroupPage ?: return
            state.value =
                oldState.copy(oldState.groupInfoForCreate.copy(icon = uriProvider))
        }

        fun updateGroupCreatePublicStatus(
            state: MutableState<CreateGroupPageState>,
            isPublic: Boolean
        ) {
            val oldState = state.value as? CreateGroupPageState.NewGroupPage ?: return
            state.value =
                oldState.copy(oldState.groupInfoForCreate.copy(isPublic = isPublic))
        }

        fun updateGroupCreateName(state: MutableState<CreateGroupPageState>, string: String) {
            val oldState = state.value as? CreateGroupPageState.NewGroupPage ?: return
            state.value = oldState.copy(oldState.groupInfoForCreate.copy(name = string))
        }

        fun updateGroupCreateMembersCount(
            state: MutableState<CreateGroupPageState>,
            string: String
        ) {
            val oldState = state.value as? CreateGroupPageState.NewGroupPage ?: return
            state.value =
                oldState.copy(oldState.groupInfoForCreate.copy(membersCount = string))
        }

        fun updateGroupCreateDescription(
            state: MutableState<CreateGroupPageState>,
            string: String
        ) {
            val oldState = state.value as? CreateGroupPageState.NewGroupPage ?: return
            state.value =
                oldState.copy(oldState.groupInfoForCreate.copy(introduction = string))
        }
    }
}