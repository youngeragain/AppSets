package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.UserInfoForModify
import xcj.app.appsets.ui.model.page_state.UserProfilePageUIState
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.server.HttpRequestFail
import xcj.app.starter.server.request
import xcj.app.starter.server.requestRaw

class UserInfoUseCase(
    private val userRepository: UserRepository,
    private val appSetsRepository: AppSetsRepository,
) : ComposeLifecycleAware {

    val currentUserInfoState: MutableState<UserProfilePageUIState> = mutableStateOf(
        UserProfilePageUIState.Loading
    )

    val loggedUserFollowedState: MutableState<Boolean> = mutableStateOf(false)

    val followedUsersState: MutableState<List<UserInfo>> = mutableStateOf(emptyList())

    val followerUsersState: MutableState<List<UserInfo>> = mutableStateOf(emptyList())

    val applicationsState: MutableState<List<Application>> = mutableStateOf(emptyList())


    private suspend fun fetchUserRelateInformation(
        userInfo: UserInfo?,
        requestOnlyUserInfo: Boolean = false,
    ) {
        if (userInfo == null) {
            currentUserInfoState.value = UserProfilePageUIState.NotFound
            return
        }
        currentUserInfoState.value = UserProfilePageUIState.LoadSuccess(userInfo)
        if (requestOnlyUserInfo) {
            return
        }
        requestRaw(
            action = {
                if (userInfo.uid != LocalAccountManager.userInfo.uid) {
                    val myFollowedThisUser = userRepository.getMyFollowedThisUser(userInfo.uid).data
                    loggedUserFollowedState.value = myFollowedThisUser == true
                } else {
                    loggedUserFollowedState.value = false
                }
                val followersByUser =
                    userRepository.getFollowersAndFollowedByUser(userInfo.uid).data
                if (!followersByUser.isNullOrEmpty()) {
                    val followers = followersByUser["followers"]
                    if (!followers.isNullOrEmpty()) {
                        followerUsersState.value = followers
                    }
                    val followed = followersByUser["followed"]
                    if (!followed.isNullOrEmpty()) {
                        followedUsersState.value = followed
                    }
                } else {
                    followerUsersState.value = emptyList()
                    followedUsersState.value = emptyList()
                }
                val applicationsByUser = appSetsRepository.getApplicationsByUser(userInfo.uid).data
                if (!applicationsByUser.isNullOrEmpty()) {
                    applicationsState.value = applicationsByUser
                } else {
                    applicationsState.value = emptyList()
                }
            })
    }

    suspend fun updateCurrentUserInfoByUid(uid: String, requestOnlyUserInfo: Boolean = false) {
        request {
            userRepository.getUserInfoByUid(uid)
        }.onSuccess { userInfo ->
            LocalAccountManager.updateUserInfoIfNeeded(userInfo)
            fetchUserRelateInformation(userInfo, requestOnlyUserInfo)
        }.onFailure { exception ->
            if (exception is HttpRequestFail) {
                when (exception.response?.code) {
                    -3 -> {
                        currentUserInfoState.value = UserProfilePageUIState.LoadFailed(
                            tips = xcj.app.appsets.R.string.login_required,
                            subTips = xcj.app.appsets.R.string.expired_information
                        )
                    }

                    else -> {
                        currentUserInfoState.value = UserProfilePageUIState.LoadFailed()
                    }
                }
            }
        }
    }

    suspend fun updateUserFollowState() {
        val userInfo =
            (currentUserInfoState.value as? UserProfilePageUIState.LoadSuccess)?.userInfo ?: return
        requestRaw(
            action = {
                if (userInfo.uid != LocalAccountManager.userInfo.uid) {
                    val myFollowedThisUser = userRepository.getMyFollowedThisUser(userInfo.uid).data
                    loggedUserFollowedState.value = myFollowedThisUser == true
                }
                val followersByUser =
                    userRepository.getFollowersAndFollowedByUser(userInfo.uid).data
                if (!followersByUser.isNullOrEmpty()) {
                    val followers = followersByUser["followers"]
                    if (!followers.isNullOrEmpty()) {
                        followerUsersState.value = followers
                    }
                    val followed = followersByUser["followed"]
                    if (!followed.isNullOrEmpty()) {
                        followedUsersState.value = followed
                    }
                }
            })
    }

    suspend fun modifyUserInfo(
        context: Context,
        userInfoForModify: UserInfoForModify
    ) {

        val currentUserInfoState = currentUserInfoState.value
        if (currentUserInfoState !is UserProfilePageUIState.LoadSuccess) {
            return
        }
        val userInfo = currentUserInfoState.userInfo

        if (!LocalAccountManager.isLoggedUser(userInfo.uid)) {
            return
        }
        request {
            userRepository.updateUserInfo(
                context,
                userInfo,
                userInfoForModify
            )
        }.onSuccess {
            updateCurrentUserInfoByUid(userInfo.uid, true)
            ContextCompat.getString(context, xcj.app.appsets.R.string.information_updated)
                .toastSuspend()
        }.onFailure {
            ContextCompat.getString(context, xcj.app.appsets.R.string.updated_failed).toastSuspend()
        }
    }

    override fun onComposeDispose(by: String?) {
        loggedUserFollowedState.value = false
        followedUsersState.value = emptyList()
        followerUsersState.value = emptyList()
        applicationsState.value = emptyList()
    }
}