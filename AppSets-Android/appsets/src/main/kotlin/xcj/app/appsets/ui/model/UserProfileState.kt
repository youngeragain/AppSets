package xcj.app.appsets.ui.model


import xcj.app.appsets.server.model.UserInfo

sealed class UserProfileState(val tips: Int?) {

    data class LoadSuccess(val userInfo: UserInfo) : UserProfileState(null)

    data object Loading :
        UserProfileState(xcj.app.appsets.R.string.loading)

    data object NotFound :
        UserProfileState(xcj.app.appsets.R.string.not_found)

}