package xcj.app.appsets.account

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.im.RabbitMqBroker
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.ktx.post
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LoginActivity
import xcj.app.core.android.ApplicationHelper
import xcj.app.io.components.SimpleFileIO
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.purple_module.MySharedPreferences


class UseOnceValue<T>(private val value: T) {
    var isUsed: Boolean = false
        private set

    fun peekValue(): T {
        return value
    }

    fun useValue(): T {
        if (isUsed)
            throw Exception("value is used!")
        isUsed = true
        return value
    }
}


object LocalAccountManager {
    var token: String? = null
        private set
    val _userInfo: MutableState<UserInfo> = mutableStateOf(UserInfo.empty())
    val _loginState: MutableState<Boolean> = mutableStateOf(false)
    val tokenErrorState: MutableState<UseOnceValue<Boolean>?> = mutableStateOf(null)

    fun isLogged(): Boolean {
        return _loginState.value
    }

    fun initPersistenceToken(token: String) {
        Log.e(
            "LocalAccountManager",
            "initPersistenceToken:\noldToken:${LocalAccountManager.token}\nnewToken:$token"
        )
        LocalAccountManager.token = token
        _loginState.value = token.isNotEmpty()
        tokenErrorState.value = null
        MySharedPreferences.putString(
            xcj.app.appsets.server.ApiDesignEncodeStr.tokenStrToMd5,
            token
        )
        if(_loginState.value)
            ModuleConstant.MSG_DELIVERY_KEY_USER_LOGIN_ACTION.post("by_new_status", 200)

    }
    fun initNonPersistenceToken(token: String?) {
        LocalAccountManager.token = token
    }

    fun restoreTokenIfNeeded(){
        if(token.isNullOrEmpty()) {
            token =
                MySharedPreferences.getString(xcj.app.appsets.server.ApiDesignEncodeStr.tokenStrToMd5)
            val userInfoInSharedPreferences = MySharedPreferences.getString("USER_INFO")
            if (!userInfoInSharedPreferences.isNullOrEmpty())
                try {
                    _userInfo.value = RabbitMqBroker.gson.fromJson(
                        userInfoInSharedPreferences,
                        UserInfo::class.java
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            _loginState.value = !token.isNullOrEmpty()
            if (_loginState.value)
                ModuleConstant.MSG_DELIVERY_KEY_USER_LOGIN_ACTION.post("by_restore_status", 200)
            Log.e("LocalAccountManager", "restoreTokenIfNeeded:token:$token")
        }
    }

    fun saveUserInfo(userInfo: UserInfo) {
        Log.e("LocalAccountManager", "saveUserInfo")
        if (!_userInfo.value.isContentSame(userInfo)) {
            if (userInfo.avatarUrl != null && !userInfo.avatarUrl.isHttpUrl())
                userInfo.avatarUrl =
                    SimpleFileIO.getInstance().generatePreSign(userInfo.avatarUrl!!)
                        ?: userInfo.avatarUrl
            _userInfo.value = userInfo
            MySharedPreferences.putString(
                "USER_INFO",
                RabbitMqBroker.gson.toJson(userInfo)
            )
        }
    }

    fun toLoginPage(context: Context) {
        LoginActivity.navigate(context)
    }

    fun userSignOuted() {
        if (token == null) {
            return
        }
        token = null
        _userInfo.value = UserInfo.empty()
        _loginState.value = false
        ApplicationHelper.coroutineScope.launch(Dispatchers.IO) {
            MySharedPreferences.clear()
            ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)?.clearAllTables()
            RabbitMqBroker.close()
            ModuleConstant.MSG_DELIVERY_KEY_USER_LOGOUT_ACTION.post(null, 200)
        }
    }

    fun isLoggedUser(uid: String): Boolean {
        return uid== _userInfo.value.uid
    }

    /**
     * @param T 当泛型拿到唯一的属性时，添加一个flag进行筛选
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> provideState(flag:Any?=null):MutableState<T> {
        return if(_userInfo.value::class == T::class){
            _userInfo as MutableState<T>
        }else if(_loginState.value::class == T::class){
            _loginState as MutableState<T>
        }else
            throw Exception("no state can provide!")
    }

    fun produceTokenError() {
        if (!_loginState.value || token.isNullOrEmpty())
            return
        val useOnceValue = tokenErrorState.value
        if (useOnceValue != null) {
            if (useOnceValue.peekValue()) {
                return
            }
        }
        userSignOuted()
        tokenErrorState.value = UseOnceValue(true)
    }

    fun isMe(uid: String): Boolean {
        return _userInfo.value.uid == uid
    }
}