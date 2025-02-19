package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xcj.app.starter.server.requestNotNull
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.GroupInfoState
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.compose_share.dynamic.IComposeDispose
import xcj.app.starter.android.util.PurpleLogger

class GroupInfoUseCase(
    private val coroutineScope: CoroutineScope,
    private val userRepository: UserRepository
) : IComposeDispose {
    companion object {
        private const val TAG = "GroupInfoUseCase"
    }

    val groupInfoState: MutableState<GroupInfoState> =
        mutableStateOf(GroupInfoState.Loading)

    fun updateGroupInfo(context: Context, groupInfo: GroupInfo?) {
        if (groupInfo == null) {
            groupInfoState.value = GroupInfoState.NotFound
            return
        }
        groupInfoState.value = GroupInfoState.LoadSuccess(groupInfo)
        if (groupInfo.userInfoList == null) {
            updateGroupInfoByGroupId(context, groupId = groupInfo.groupId)
        }
    }

    private fun updateGroupInfoByGroupId(context: Context, groupId: String?) {
        if (groupId.isNullOrEmpty()) {
            groupInfoState.value = GroupInfoState.NotFound
            return
        }
        groupInfoState.value = GroupInfoState.Loading
        coroutineScope.launch {
            requestNotNull(
                action = {
                    userRepository.getGroupInfoById(groupId)
                },
                onSuccess = { groupInfo ->
                    PictureUrlMapper.mapPictureUrl(groupInfo)
                    groupInfoState.value = GroupInfoState.LoadSuccess(groupInfo)
                },
                onFailed = {
                    groupInfoState.value = GroupInfoState.NotFound
                    PurpleLogger.current.d(
                        TAG,
                        "updateGroupInfoByGroupId failed:${it.e?.message}"
                    )
                }
            )
        }
    }

    override fun onComposeDispose(by: String?) {

    }
}