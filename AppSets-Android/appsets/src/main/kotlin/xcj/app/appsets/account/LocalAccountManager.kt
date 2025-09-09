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
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.model.state.AccountStatus
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.ApiDesignKeys
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

    val accountStatus: MutableState<AccountStatus> =
        mutableStateOf(AccountStatus.NotLogged())

    private var appToken: String? = null

    val token: String?
        get() {
            val statusState = accountStatus.value
            if (statusState is AccountStatus.Logged) {
                return statusState.token
            } else if (statusState is AccountStatus.TempLogged) {
                return statusState.token
            }
            return null
        }

    val userInfo: UserInfo
        get() {
            return accountStatus.value.userInfo
        }

    fun provideAppToken(): String? {
        return appToken
    }

    fun saveAppToken(token: String?) {
        appToken = token
        LocalMessenger.post(MESSAGE_KEY_ON_APP_TOKEN_GOT, token)
    }

    fun isLogged(): Boolean {
        return accountStatus.value is AccountStatus.Logged
    }

    suspend fun restoreLoginStatusStateIfNeeded() {
        PurpleLogger.current.d(TAG, "restoreLoginStateIfNeeded")
        if (accountStatus.value is AccountStatus.Logged) {
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
            accountStatus.value = AccountStatus.TempLogged(userInfo, token)
            return
        }

        accountStatus.value = AccountStatus.Logged(userInfo, token, isFromLocal)

        if (isFromLocal) {
            LocalMessenger.post(MESSAGE_KEY_ON_LOGIN, LOGIN_BY_RESTORE, 600)
        } else {
            saveToken(token)
            saveUserInfo(userInfo, "onUserLogged")
            LocalMessenger.post(MESSAGE_KEY_ON_LOGIN, LOGIN_BY_NEW, 200)
        }
    }

    fun onUserLogout(by: String = LOGOUT_BY_MANUALLY) {
        PurpleLogger.current.d(TAG, "onUserLogout, by:$by")
        accountStatus.value = AccountStatus.NotLogged()
        LocalPurpleCoroutineScope.current.launch(Dispatchers.IO) {
            MySharedPreferences.clear()
            ModuleHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)?.clearAllTables()
            BrokerTest.close()
            LocalMessenger.post(MESSAGE_KEY_ON_LOGOUT, by, 200)
        }
    }

    fun isLoggedUser(uid: String?): Boolean {
        return uid == (accountStatus.value as? AccountStatus.Logged)?.userInfo?.uid
    }

    fun produceTokenError() {
        if (accountStatus.value !is AccountStatus.Logged) {
            return
        }
        accountStatus.value = AccountStatus.Expired()
        onUserLogout(LOGOUT_BY_TOKEN_EXPIRE)
    }

    fun updateUserInfoIfNeeded(userInfo: UserInfo) {
        if (!isLogged()) {
            return
        }
        if (!isLoggedUser(userInfo.uid)) {
            return
        }
        if (UserInfo.isContentSame(accountStatus.value.userInfo, userInfo)) {
            return
        }
        accountStatus.value =
            (accountStatus.value as AccountStatus.Logged).copy(userInfo = userInfo)
        saveUserInfo(userInfo, "updateUserInfoIfNeeded")
    }
}