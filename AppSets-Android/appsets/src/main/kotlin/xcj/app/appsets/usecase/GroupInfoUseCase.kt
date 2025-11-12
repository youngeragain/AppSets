package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.page_state.GroupInfoPageUIState
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.request

class GroupInfoUseCase(
    private val userRepository: UserRepository,
) : ComposeLifecycleAware {
    companion object {
        private const val TAG = "GroupInfoUseCase"
    }

    val groupInfoPageUIState: MutableState<GroupInfoPageUIState> =
        mutableStateOf(GroupInfoPageUIState.Loading)

    suspend fun updateGroupInfo(context: Context, groupInfo: GroupInfo?) {
        if (groupInfo == null) {
            groupInfoPageUIState.value = GroupInfoPageUIState.NotFound
            return
        }
        groupInfoPageUIState.value = GroupInfoPageUIState.LoadSuccess(groupInfo)
        if (groupInfo.userInfoList == null) {
            updateGroupInfoByGroupId(context, groupId = groupInfo.groupId)
        }
    }

    private suspend fun updateGroupInfoByGroupId(context: Context, groupId: String?) {
        if (groupId.isNullOrEmpty()) {
            groupInfoPageUIState.value = GroupInfoPageUIState.NotFound
            return
        }
        groupInfoPageUIState.value = GroupInfoPageUIState.Loading
        request {
            userRepository.getGroupInfoById(groupId)
        }.onSuccess { groupInfo ->
            PictureUrlMapper.mapPictureUrl(groupInfo)
            groupInfoPageUIState.value = GroupInfoPageUIState.LoadSuccess(groupInfo)
        }.onFailure {
            groupInfoPageUIState.value = GroupInfoPageUIState.NotFound
            PurpleLogger.current.d(
                TAG,
                "updateGroupInfoByGroupId failed:${it.message}"
            )
        }
    }

    override fun onComposeDispose(by: String?) {

    }
}