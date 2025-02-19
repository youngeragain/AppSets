package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import xcj.app.appsets.util.model.UriProvider

data class SignUpUserInfo(
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
        fun updateStateUserAccount(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(account = string))
        }

        fun updateStateUserPassword(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(password = string))
        }

        fun updateStateUserName(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userName = string))
        }

        fun updateStateUserIntroduction(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userIntroduction = string))
        }

        fun updateStateUserTags(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userTags = string))
        }

        fun updateStateUserSex(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userSex = string))
        }

        fun updateStateUserAge(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userAge = string))
        }

        fun updateStateUserPhone(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userPhone = string))
        }

        fun updateStateUserEmail(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userEmail = string))
        }

        fun updateStateUserArea(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userArea = string))
        }

        fun updateStateUserAddress(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userAddress = string))
        }

        fun updateStateUserWebsite(state: MutableState<LoginSignUpState>, string: String) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userWebsite = string))
        }

        fun updateStateUserAvatar(state: MutableState<LoginSignUpState>, uriProvider: UriProvider) {
            val oldState = state.value as? LoginSignUpState.SignUp ?: return
            state.value =
                oldState.copy(signUpUserInfo = oldState.signUpUserInfo.copy(userAvatar = uriProvider))
        }
    }
}