package xcj.app.appsets.usecase

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.ktx.requestRaw
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.ktx.toastSuspend
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.repository.UserLoginRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.nonecompose.ui.dialog.UserAgreementBottomSheetDialog
import xcj.app.appsets.util.Md5Helper
import xcj.app.core.foundation.usecase.NoConfigUseCase

class UserLoginUseCase(
    private val coroutineScope: CoroutineScope,
    private val loginSignUpState: MutableState<LoginSignUpState?>,
): NoConfigUseCase() {

    sealed class LoginSignUpState {
        object Default : LoginSignUpState()
        class Logining(val tips: String? = null) : LoginSignUpState()
        object LoggingFinish : LoginSignUpState()
        class LoggingFail(val tips: String? = null) : LoginSignUpState()
        class SignUping(val tips: String? = null) : LoginSignUpState()
        object SignUpFinish : LoginSignUpState()
        data class SignUpFail(val tips: String? = null) : LoginSignUpState()
    }


    fun login(
        context: Context,
        account: String,
        password: String
    ) {
        if (loginSignUpState.value is LoginSignUpState.Logining)
            return
        if (account.isEmpty()) {
            "请输入账号".toast()
            return
        }
        if (password.isEmpty()) {
            "请输入密码".toast()
            return
        }
        loginSignUpState.value = LoginSignUpState.Logining()
        coroutineScope.requestRaw({
            Log.i("UserLoginUseCase", "login action thread:${Thread.currentThread()}")
            val accountEncode = Md5Helper.encode(account)
            val passwordEncode = Md5Helper.encode(password)
            val userLoginRepository = UserLoginRepository(URLApi.provide(UserApi::class.java))
            val loginRes = userLoginRepository.login(accountEncode, passwordEncode)
            if (!loginRes.success || loginRes.data.isNullOrEmpty()) {
                Log.i("UserLoginUseCase", "登录失败, token获取失败!")
                loginSignUpState.value = LoginSignUpState.LoggingFail()
                if (!loginRes.info.isNullOrEmpty())
                    loginRes.info.toastSuspend()
                return@requestRaw
            }
            LocalAccountManager.initNonPersistenceToken(loginRes.data)
            val userInfoRepository: UserRepository =
                UserRepository(URLApi.provide(UserApi::class.java))
            val userInfoRes = userInfoRepository.getLoggedUserInfo()
            if (!userInfoRes.success || userInfoRes.data == null) {
                Log.i("UserLoginUseCase", "登录失败, userInfo isNullOrEmpty!")
                loginSignUpState.value = LoginSignUpState.LoggingFail()
                return@requestRaw
            }
            val agree = userInfoRes.data!!.agreeToTheAgreement == 1
            if (agree) {
                LocalAccountManager.saveUserInfo(userInfoRes.data!!)
                LocalAccountManager.initPersistenceToken(loginRes.data!!)
                loginSignUpState.value = LoginSignUpState.LoggingFinish
                return@requestRaw
            }
            val supportFragmentManager1 =
                (context as? AppCompatActivity)?.supportFragmentManager
            Log.i(
                "UserLoginUseCase",
                "context:${context} supportFragmentManager1$supportFragmentManager1"
            )
            val supportFragmentManager = supportFragmentManager1 ?: return@requestRaw
            val userAgreementBottomSheetDialog = UserAgreementBottomSheetDialog().apply {
                arguments = bundleOf("full_height" to true, "title" to "AppSets的政策")
                onClick = { dialogFragment: DialogFragment, accept: Boolean ->
                    if (accept) {
                        Log.i("UserLoginUseCase", "同意AppSets的政策")
                        dialogFragment.dismiss()
                        LocalAccountManager.saveUserInfo(userInfoRes.data!!)
                        LocalAccountManager.initPersistenceToken(loginRes.data!!)
                        loginSignUpState.value = LoginSignUpState.LoggingFinish
                    } else {
                        coroutineScope.requestRaw({
                            LocalAccountManager.userSignOuted()
                            userLoginRepository.signOut()
                        })
                        loginSignUpState.value = LoginSignUpState.LoggingFinish
                        Log.i("UserLoginUseCase", "不同意AppSets的政策, 无法使用AppSets")
                    }
                }
            }
            userAgreementBottomSheetDialog.show(
                supportFragmentManager,
                userAgreementBottomSheetDialog.tag
            )
        }, onFailed = {
            loginSignUpState.value = LoginSignUpState.LoggingFail()
            Log.e("UserLoginUseCase", "login failed")
        })
    }

    fun toLoginPageOrSignOut(context: Context) {
        if (!LocalAccountManager.isLogged()) {
            LocalAccountManager.toLoginPage(context)
            return
        }
        coroutineScope.requestNotNull({
            LocalAccountManager.userSignOuted()
            val userLoginRepository = UserLoginRepository(URLApi.provide(UserApi::class.java))
            userLoginRepository.signOut()
        })
    }




    fun clean() {

    }

}