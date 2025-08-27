package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import xcj.app.appsets.util.model.UriProvider

data class UserInfoForModify(
    val userName: String = "",
    val userIntroduction: String = "",
    val userTags: String = "",
    val userSex: String = "",
    val userAge: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val userArea: String = "",
    val userAddress: String = "",
    val userWebsite: String = "",
    val userAvatarUri: UriProvider? = null,
) {
    companion object {
        fun updateStateUserName(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userName = string)
        }

        fun updateStateUserIntroduction(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userIntroduction = string)
        }

        fun updateStateUserTags(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userTags = string)
        }

        fun updateStateUserSex(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userSex = string)
        }

        fun updateStateUserAge(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userAge = string)
        }

        fun updateStateUserPhone(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userPhone = string)
        }

        fun updateStateUserEmail(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userEmail = string)
        }

        fun updateStateUserArea(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userArea = string)
        }

        fun updateStateUserAddress(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userAddress = string)
        }

        fun updateStateUserWebsite(state: MutableState<UserInfoForModify>, string: String) {
            state.value = state.value.copy(userWebsite = string)
        }

        fun updateStateUserAvatarUri(
            state: MutableState<UserInfoForModify>,
            uriProvider: UriProvider
        ) {
            state.value = state.value.copy(userAvatarUri = uriProvider)
        }
    }
}