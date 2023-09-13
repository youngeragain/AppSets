package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.ktx.requestNotNullRaw
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.usecase.models.Application

sealed class UserInfoProfileState {
    data class UserInfoWrapper(val userInfo: UserInfo?) : UserInfoProfileState()
    data class Loading(val tips: String) : UserInfoProfileState()

    data class None(val tips: String) : UserInfoProfileState()
}

class UserInfoUseCase(
    private val coroutineScope: CoroutineScope
) : DisposeUseCase() {
    private val userInfoRepository: UserRepository =
        UserRepository(URLApi.provide(UserApi::class.java))


    val currentUserInfoState: MutableState<UserInfoProfileState> = mutableStateOf(
        UserInfoProfileState.Loading("加载中")
    )

    val myFollowedState: MutableState<Boolean> = mutableStateOf(false)

    val followedUserListState: MutableState<List<UserInfo>?> = mutableStateOf(null)
    val followerUserListState: MutableState<List<UserInfo>?> = mutableStateOf(null)

    val applicationsForThisUserState: MutableState<List<Application>?> = mutableStateOf(null)

    private fun updateUserInfo(userInfo: UserInfo?) {
        onDispose()
        currentUserInfoState.value = UserInfoProfileState.UserInfoWrapper(userInfo)
        if (userInfo != null) {
            coroutineScope.requestNotNullRaw({
                if (userInfo.uid != LocalAccountManager._userInfo.value.uid) {
                    val myFollowedThisUser =
                        userInfoRepository.getMyFollowedThisUser(userInfo.uid).data
                    myFollowedState.value = myFollowedThisUser == true
                }
                val followersByUser =
                    userInfoRepository.getFollowersAndFollowedByUser(userInfo.uid).data
                if (!followersByUser.isNullOrEmpty()) {
                    val followers = followersByUser["followers"]
                    if (!followers.isNullOrEmpty())
                        followerUserListState.value = followers
                    val followed = followersByUser["followed"]
                    if (!followed.isNullOrEmpty())
                        followedUserListState.value = followed
                }
                val applicationsByUser =
                    AppSetsRepository.getInstance().getApplicationsByUser(userInfo.uid).data
                if (!applicationsByUser.isNullOrEmpty()) {
                    applicationsForThisUserState.value = applicationsByUser
                }
            })
        }
    }

    /*  fun getApplicationsForThisUser(){
          val uid = (currentUserInfoState.value as? UserInfoProfileState.UserInfoWrapper)?.userInfo?.uid?:return
          coroutineScope.request( {
              AppSetsRepository.getInstance().getApplicationsByUser(uid)
          }, onSuccess = {
              if(!it.isNullOrEmpty()){
                  applicationsForThisUserState.value = it
              }
          })
      }
  */
    fun updateUserInfoByUid(uid: String?) {
        if (uid.isNullOrEmpty())
            return
        coroutineScope.requestNotNull({
            userInfoRepository.getUserInfoByUid(uid)
        }, onSuccess = {
            updateUserInfo(it)
        })
    }

    override fun onDispose() {
        currentUserInfoState.value = UserInfoProfileState.Loading("加载中")
        myFollowedState.value = false
        followedUserListState.value = null
        followerUserListState.value = null
        applicationsForThisUserState.value = null
    }

    fun updateUserFollowState() {
        val userInfo =
            (currentUserInfoState.value as? UserInfoProfileState.UserInfoWrapper)?.userInfo
                ?: return
        coroutineScope.requestNotNullRaw({
            if (userInfo.uid != LocalAccountManager._userInfo.value.uid) {
                val myFollowedThisUser =
                    userInfoRepository.getMyFollowedThisUser(userInfo.uid).data
                myFollowedState.value = myFollowedThisUser == true
            }
            val followersByUser =
                userInfoRepository.getFollowersAndFollowedByUser(userInfo.uid).data
            if (!followersByUser.isNullOrEmpty()) {
                val followers = followersByUser["followers"]
                if (!followers.isNullOrEmpty())
                    followerUserListState.value = followers
                val followed = followersByUser["followed"]
                if (!followed.isNullOrEmpty())
                    followedUserListState.value = followed
            }
        })
    }
}