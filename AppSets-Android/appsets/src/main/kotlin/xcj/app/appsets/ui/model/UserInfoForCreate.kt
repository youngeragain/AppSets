package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import xcj.app.appsets.ui.model.page_state.LoginSignUpPageState
import xcj.app.appsets.util.model.UriProvider

data class UserInfoForCreate(
    val account: String = "",
    val password: String = "",
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
    val userAvatar: UriProvider? = null,
) {
    companion object {
        fun updateStateUserAccount(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(account = string))
        }

        fun updateStateUserPassword(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(password = string))
        }

        fun updateStateUserName(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userName = string))
        }

        fun updateStateUserIntroduction(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userIntroduction = string))
        }

        fun updateStateUserTags(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userTags = string))
        }

        fun updateStateUserSex(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userSex = string))
        }

        fun updateStateUserAge(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userAge = string))
        }

        fun updateStateUserPhone(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userPhone = string))
        }

        fun updateStateUserEmail(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userEmail = string))
        }

        fun updateStateUserArea(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userArea = string))
        }

        fun updateStateUserAddress(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userAddress = string))
        }

        fun updateStateUserWebsite(state: MutableState<LoginSignUpPageState>, string: String) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userWebsite = string))
        }

        fun updateStateUserAvatar(
            state: MutableState<LoginSignUpPageState>,
            uriProvider: UriProvider
        ) {
            val oldState = state.value as? LoginSignUpPageState.SignUpStart ?: return
            state.value =
                oldState.copy(userInfoForCreate = oldState.userInfoForCreate.copy(userAvatar = uriProvider))
        }
    }
}