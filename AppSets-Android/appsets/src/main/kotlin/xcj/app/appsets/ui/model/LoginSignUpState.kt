package xcj.app.appsets.ui.model

sealed class LoginSignUpState(val tips: Int?) {

    data object Nothing : LoginSignUpState(null)

    data object Logging : LoginSignUpState(null)

    data object LoggingFinish : LoginSignUpState(null)

    data object LoggingFail : LoginSignUpState(null)

    data class SignUp(val signUpUserInfo: SignUpUserInfo) : LoginSignUpState(null)

    data class SignUping(val signUpUserInfo: SignUpUserInfo) : LoginSignUpState(null)

    data class SignUpFinish(val signUpUserInfo: SignUpUserInfo) : LoginSignUpState(null)

    class SignUpFail(val signUpUserInfo: SignUpUserInfo, tips: Int) : LoginSignUpState(tips)

}