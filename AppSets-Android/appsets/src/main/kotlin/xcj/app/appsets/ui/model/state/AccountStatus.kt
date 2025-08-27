package xcj.app.appsets.ui.model.state

import xcj.app.appsets.server.model.UserInfo

sealed interface AccountStatus {
    val userInfo: UserInfo

    data class Logged(
        override val userInfo: UserInfo,
        val token: String,
        val isFromLocal: Boolean
    ) : AccountStatus

    data class TempLogged(
        override val userInfo: UserInfo,
        val token: String
    ) : AccountStatus

    data class NotLogged(
        override val userInfo: UserInfo = UserInfo.Companion.default()
    ) : AccountStatus

    data class Expired(
        override val userInfo: UserInfo = UserInfo.Companion.default()
    ) : AccountStatus

    data class LoggingIn(
        override val userInfo: UserInfo
    ) : AccountStatus
}