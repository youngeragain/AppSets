package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.UserInfoForModify
import xcj.app.appsets.ui.model.page_state.UserProfilePageState
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestNotNullRaw

class UserInfoUseCase(
    private val userRepository: UserRepository,
    private val appSetsRepository: AppSetsRepository,
) : IComposeLifecycleAware {

    val currentUserInfoState: MutableState<UserProfilePageState> = mutableStateOf(
        UserProfilePageState.Loading
    )

    val loggedUserFollowedState: MutableState<Boolean> = mutableStateOf(false)

    val followedUsersState: MutableState<List<UserInfo>> = mutableStateOf(emptyList())

    val followerUsersState: MutableState<List<UserInfo>> = mutableStateOf(emptyList())

    val applicationsState: MutableState<List<Application>> = mutableStateOf(emptyList())

    val userInfoForModifyState: MutableState<UserInfoForModify> = mutableStateOf(
        UserInfoForModify()
    )

    private suspend fun fetchUserRelateInformation(
        userInfo: UserInfo?,
        requestOnlyUserInfo: Boolean = false,
    ) {
        if (userInfo == null) {
            currentUserInfoState.value = UserProfilePageState.NotFound
            return
        }
        currentUserInfoState.value = UserProfilePageState.LoadSuccess(userInfo)
        if (requestOnlyUserInfo) {
            return
        }
        requestNotNullRaw(
            action = {
                if (userInfo.uid != LocalAccountManager.userInfo.uid) {
                    val myFollowedThisUser =
                        userRepository.getMyFollowedThisUser(userInfo.uid).data
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
                val applicationsByUser =
                    appSetsRepository.getApplicationsByUser(userInfo.uid).data
                if (!applicationsByUser.isNullOrEmpty()) {
                    applicationsState.value = applicationsByUser
                } else {
                    applicationsState.value = emptyList()
                }
            }
        )
    }

    suspend fun updateCurrentUserInfoByUid(uid: String, requestOnlyUserInfo: Boolean = false) {
        requestNotNull(
            action = {
                userRepository.getUserInfoByUid(uid)
            },
            onSuccess = {
                LocalAccountManager.updateUserInfoIfNeeded(it)
                fetchUserRelateInformation(it, requestOnlyUserInfo)
            }
        )
    }

    suspend fun updateUserFollowState() {
        val userInfo =
            (currentUserInfoState.value as? UserProfilePageState.LoadSuccess)?.userInfo
                ?: return
        requestNotNullRaw(
            action = {
                if (userInfo.uid != LocalAccountManager.userInfo.uid) {
                    val myFollowedThisUser =
                        userRepository.getMyFollowedThisUser(userInfo.uid).data
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
            }
        )
    }

    suspend fun modifyUserInfo(
        context: Context,
    ) {

        val currentUserInfoState = currentUserInfoState.value
        if (currentUserInfoState !is UserProfilePageState.LoadSuccess) {
            return
        }
        val userInfo = currentUserInfoState.userInfo

        if (!LocalAccountManager.isLoggedUser(userInfo.uid)) {
            return
        }
        val userInfoModification = userInfoForModifyState.value
        requestNotNull(
            action = {
                userRepository.updateUserInfo(
                    context,
                    userInfo,
                    userInfoModification
                )
            },
            onSuccess = {
                updateCurrentUserInfoByUid(userInfo.uid, true)
                context.getString(xcj.app.appsets.R.string.information_updated).toastSuspend()
            },
            onFailed = {
                context.getString(xcj.app.appsets.R.string.updated_failed).toastSuspend()
            }
        )
    }

    fun updateUserSelectAvatarUri(uriProvider: UriProvider) {
        UserInfoForModify.updateStateUserAvatarUri(userInfoForModifyState, uriProvider)
    }

    override fun onComposeDispose(by: String?) {
        loggedUserFollowedState.value = false
        followedUsersState.value = emptyList()
        followerUsersState.value = emptyList()
        applicationsState.value = emptyList()
        userInfoForModifyState.value = UserInfoForModify()
    }
}