package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.page_state.GroupInfoPageState
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

    val groupInfoPageState: MutableState<GroupInfoPageState> =
        mutableStateOf(GroupInfoPageState.Loading)

    suspend fun updateGroupInfo(context: Context, groupInfo: GroupInfo?) {
        if (groupInfo == null) {
            groupInfoPageState.value = GroupInfoPageState.NotFound
            return
        }
        groupInfoPageState.value = GroupInfoPageState.LoadSuccess(groupInfo)
        if (groupInfo.userInfoList == null) {
            updateGroupInfoByGroupId(context, groupId = groupInfo.groupId)
        }
    }

    private suspend fun updateGroupInfoByGroupId(context: Context, groupId: String?) {
        if (groupId.isNullOrEmpty()) {
            groupInfoPageState.value = GroupInfoPageState.NotFound
            return
        }
        groupInfoPageState.value = GroupInfoPageState.Loading
        request {
            userRepository.getGroupInfoById(groupId)
        }.onSuccess { groupInfo ->
            PictureUrlMapper.mapPictureUrl(groupInfo)
            groupInfoPageState.value = GroupInfoPageState.LoadSuccess(groupInfo)
        }.onFailure {
            groupInfoPageState.value = GroupInfoPageState.NotFound
            PurpleLogger.current.d(
                TAG,
                "updateGroupInfoByGroupId failed:${it.message}"
            )
        }
    }

    override fun onComposeDispose(by: String?) {

    }
}