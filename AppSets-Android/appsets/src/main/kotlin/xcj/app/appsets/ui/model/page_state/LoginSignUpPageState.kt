package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.appsets.ui.model.UserInfoForCreate

sealed interface LoginSignUpPageState : TipsProvider {

    data object LoginStart : LoginSignUpPageState {
        override val tips: Int? = null
        override val subTips: Int? = null
    }

    data class Logging(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

    data class LoggingFinish(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

    data class LoggingFailed(
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

    data class SignUpStart(
        val userInfoForCreate: UserInfoForCreate,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

    data class SignUpping(
        val userInfoForCreate: UserInfoForCreate,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

    data class SignUpFinish(
        val userInfoForCreate: UserInfoForCreate,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

    class SignUpPageFailed(
        val userInfoForCreate: UserInfoForCreate,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : LoginSignUpPageState

}