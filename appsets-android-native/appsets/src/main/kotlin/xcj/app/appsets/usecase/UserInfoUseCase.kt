package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.core.foundation.usecase.NoConfigUseCase

sealed class UserInfoProfileState {
    data class UserInfoWrapper(val userInfo: UserInfo?) : UserInfoProfileState()
    data class Loading(val tips: String) : UserInfoProfileState()
}

class UserInfoUseCase(
    private val coroutineScope: CoroutineScope
) : NoConfigUseCase() {
    private val userInfoRepository: UserRepository =
        UserRepository(URLApi.provide(UserApi::class.java))


    val currentUserInfoState: MutableState<UserInfoProfileState> = mutableStateOf(
        UserInfoProfileState.Loading("加载中")
    )

    fun updateUserInfo(userInfo: UserInfo?) {
        currentUserInfoState.value = UserInfoProfileState.UserInfoWrapper(userInfo)
    }

    fun updateUserInfoByUid(uid: String) {
        coroutineScope.requestNotNull({
            userInfoRepository.getUserInfoByUid(uid)
        }, onSuccess = {
            currentUserInfoState.value = UserInfoProfileState.UserInfoWrapper(it)
        })
    }

}