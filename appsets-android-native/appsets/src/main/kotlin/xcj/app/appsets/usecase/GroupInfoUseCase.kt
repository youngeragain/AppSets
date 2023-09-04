package xcj.app.appsets.usecase

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.db.room.repository.UserGroupsRoomRepository
import xcj.app.appsets.db.room.repository.UserInfoRoomRepository
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.repository.UserRepository

sealed class GroupInfoState {
    data class GroupInfoWrapper(val groupInfo: GroupInfo) : GroupInfoState()
    data class Loading(val tips: String) : GroupInfoState()
}

class GroupInfoUseCase(private val coroutineScope: CoroutineScope) {
    private val TAG = "GroupInfoUseCase"

    private val userRepository: UserRepository by lazy {
        UserRepository(URLApi.provide(UserApi::class.java))
    }

    val groupInfoState: MutableState<GroupInfoState?> =
        mutableStateOf(null)

    fun updateGroupInfo(groupInfo: GroupInfo?) {
        if (groupInfo == null) {
            groupInfoState.value = null
            return
        }
        if (groupInfoState.value == null || groupInfoState.value !is GroupInfoState.Loading) {
            groupInfoState.value = GroupInfoState.Loading("加载中")
        }
        groupInfoState.value = GroupInfoState.GroupInfoWrapper(groupInfo)
    }

    fun updateGroupInfoByGroupId(groupId: String?) {
        if (groupId.isNullOrEmpty()) {
            groupInfoState.value = null
            return
        }
        if (groupInfoState.value == null || groupInfoState.value !is GroupInfoState.Loading) {
            groupInfoState.value = GroupInfoState.Loading("加载中")
        }
        coroutineScope.requestNotNull({
            userRepository.getGroupInfoById(groupId)
        }, onSuccess = {
            if (it == null) {
                return@requestNotNull
            }
            UserGroupsRoomRepository.mapIconUrl(listOf(it))
            it.userInfoList?.let { userInfoList -> UserInfoRoomRepository.mapAvatarUrl(userInfoList) }
            groupInfoState.value = GroupInfoState.GroupInfoWrapper(it)
        }, onFailed = {
            Log.e(TAG, "updateGroupInfoByGroupId failed:${it.e}")
        })
    }
}