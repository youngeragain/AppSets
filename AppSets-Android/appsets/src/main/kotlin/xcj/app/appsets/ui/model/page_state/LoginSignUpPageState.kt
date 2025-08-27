package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.appsets.ui.model.UserInfoForCreate

sealed interface LoginSignUpPageState : TipsProvider {

    data object Nothing : LoginSignUpPageState {
        override val tipsIntRes: Int? = null
    }

    data class Logging(
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

    data class LoggingFinish(
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

    data class LoggingFail(
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

    data class SignUpPage(
        val userInfoForCreate: UserInfoForCreate,
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

    data class SignUpingPage(
        val userInfoForCreate: UserInfoForCreate,
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

    data class SignUpPageFinish(
        val userInfoForCreate: UserInfoForCreate,
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

    class SignUpPageFail(
        val userInfoForCreate: UserInfoForCreate,
        override val tipsIntRes: Int? = null
    ) : LoginSignUpPageState

}