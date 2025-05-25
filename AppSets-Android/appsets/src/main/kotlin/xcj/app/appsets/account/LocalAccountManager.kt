package xcj.app.appsets.account

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.im.BrokerTest
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.purple_module.MySharedPreferences
import xcj.app.starter.server.ApiDesignKeys
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.model.LoginStatusState
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalPurpleCoroutineScope

object LocalAccountManager {
    private const val TAG = "LocalAccountManager"

    private const val SPK_USER_INFO = "USER_INFO"

    const val MESSAGE_KEY_ON_APP_TOKEN_GOT = "on_app_token_got"

    const val LOGIN_BY_NEW = "by_new"
    const val LOGIN_BY_RESTORE = "by_restore_status"
    const val LOGOUT_BY_MANUALLY = "by_manually"
    const val LOGOUT_BY_TOKEN_EXPIRE = "by_token_expire"

    //当用户登录后
    const val MESSAGE_KEY_ON_LOGIN = "on_login"

    //当用户退出登录后
    const val MESSAGE_KEY_ON_LOGOUT = "on_logout"

    val loginStatusState: MutableState<LoginStatusState> =
        mutableStateOf(LoginStatusState.NotLogged())

    private var appToken: String? = null

    val token: String?
        get() {
            val statusState = loginStatusState.value
            if (statusState is LoginStatusState.Logged) {
                return statusState.token
            } else if (statusState is LoginStatusState.TempLogged) {
                return statusState.token
            }
            return null
        }

    val userInfo: UserInfo
        get() {
            return loginStatusState.value.userInfo
        }

    fun provideAppToken(): String? {
        return appToken
    }

    fun saveAppToken(token: String?) {
        appToken = token
        LocalMessager.post(MESSAGE_KEY_ON_APP_TOKEN_GOT, token)
    }

    fun isLogged(): Boolean {
        return loginStatusState.value is LoginStatusState.Logged
    }

    suspend fun restoreLoginStatusStateIfNeeded() {
        PurpleLogger.current.d(TAG, "restoreLoginStateIfNeeded")
        if (loginStatusState.value is LoginStatusState.Logged) {
            PurpleLogger.current.d(TAG, "restoreLoginStateIfNeeded, already logged, return")
            return
        }
        val token =
            MySharedPreferences.getString(ApiDesignKeys.TOKEN_MD5)
        if (token.isNullOrEmpty()) {
            PurpleLogger.current.d(TAG, "restoreLoginStateIfNeeded, token is nullOrEmpty, return")
            return
        }
        PurpleLogger.current.d(TAG, "restoreLoginStateIfNeeded, token:$token")

        val userInfoJsonString = MySharedPreferences.getString(SPK_USER_INFO)
        runCatching {
            val userInfo = Gson().fromJson(
                userInfoJsonString,
                UserInfo::class.java
            )
            PictureUrlMapper.mapPictureUrl(userInfo)
            onUserLogged(userInfo, token, false, true)
        }
    }

    private fun saveUserInfo(userInfo: UserInfo, by: String) {
        PurpleLogger.current.d(TAG, "saveUserInfo, by:$by")
        MySharedPreferences.putString(
            SPK_USER_INFO,
            Gson().toJson(userInfo)
        )
    }

    private fun saveToken(token: String) {
        PurpleLogger.current.d(TAG, "saveToken")
        MySharedPreferences.putString(
            ApiDesignKeys.TOKEN_MD5,
            token
        )
    }

    fun onUserLogged(
        userInfo: UserInfo,
        token: String,
        isTemp: Boolean,
        isFromLocal: Boolean = false
    ) {
        PurpleLogger.current.d(
            TAG,
            "onUserLogged, isTemp:$isTemp, isFromLocal:$isFromLocal, userInfo:$userInfo, bioUrl:${userInfo.bioUrl}"
        )

        if (isTemp) {
            loginStatusState.value = LoginStatusState.TempLogged(userInfo, token)
            return
        }

        loginStatusState.value = LoginStatusState.Logged(userInfo, token, isFromLocal)

        if (isFromLocal) {
            LocalMessager.post(MESSAGE_KEY_ON_LOGIN, LOGIN_BY_RESTORE, 600)
        } else {
            saveToken(token)
            saveUserInfo(userInfo, "onUserLogged")
            LocalMessager.post(MESSAGE_KEY_ON_LOGIN, LOGIN_BY_NEW, 200)
        }
    }

    fun onUserLogout(by: String = LOGOUT_BY_MANUALLY) {
        PurpleLogger.current.d(TAG, "onUserLogout, by:$by")
        loginStatusState.value = LoginStatusState.NotLogged()
        LocalPurpleCoroutineScope.current.launch(Dispatchers.IO) {
            MySharedPreferences.clear()
            ModuleHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)?.clearAllTables()
            BrokerTest.close()
            LocalMessager.post(MESSAGE_KEY_ON_LOGOUT, by, 200)
        }
    }

    fun isLoggedUser(uid: String?): Boolean {
        return uid == (loginStatusState.value as? LoginStatusState.Logged)?.userInfo?.uid
    }

    fun produceTokenError() {
        if (loginStatusState.value !is LoginStatusState.Logged) {
            return
        }
        loginStatusState.value = LoginStatusState.Expired()
        onUserLogout(LOGOUT_BY_TOKEN_EXPIRE)
    }

    fun updateUserInfoIfNeeded(userInfo: UserInfo) {
        if (!isLogged()) {
            return
        }
        if (!isLoggedUser(userInfo.uid)) {
            return
        }
        if (UserInfo.isContentSame(loginStatusState.value.userInfo, userInfo)) {
            return
        }
        loginStatusState.value =
            (loginStatusState.value as LoginStatusState.Logged).copy(info = userInfo)
        saveUserInfo(userInfo, "updateUserInfoIfNeeded")
    }
}