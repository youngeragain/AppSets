package xcj.app.appsets.ui.model

import xcj.app.appsets.server.model.UserInfo

sealed class LoginStatusState(val userInfo: UserInfo) {

    data class Logged(var info: UserInfo, val token: String, val isFromLocal: Boolean) :
        LoginStatusState(info)

    data class TempLogged(val info: UserInfo, val token: String) : LoginStatusState(info)

    data class NotLogged(val info: UserInfo = UserInfo.default()) : LoginStatusState(info)

    data class Expired(val info: UserInfo = UserInfo.default()) : LoginStatusState(info)

    data class LoggingIn(val info: UserInfo) : LoginStatusState(info)
}