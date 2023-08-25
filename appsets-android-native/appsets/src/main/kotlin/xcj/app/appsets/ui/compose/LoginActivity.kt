package xcj.app.appsets.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.observeAny2
import xcj.app.appsets.ui.compose.login.LoginPage
import xcj.app.appsets.ui.compose.login.SignUpPage
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.nonecompose.base.BaseActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.purple_module.ModuleConstant


class LoginActivity :
    BaseActivity<ViewDataBinding, LoginViewModel, BaseViewModelFactory<LoginViewModel>>() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, PageRouteNameProvider.LoginPage) {
                    composable(PageRouteNameProvider.LoginPage) {
                        LoginPage(
                            loginSignUpState = viewModel?.systemUseCase!!.loginSignUpState,
                            qrCodeState = viewModel?.qrCodeUseCase!!.qrcodeState,
                            onSignUpButtonClick = {
                                navController.navigate(PageRouteNameProvider.SignUpPage)
                            },
                            onQRCodeLoginButtonClick = {
                                viewModel?.qrCodeUseCase?.genQrCode()
                            },
                            onScanQRCodeButtonClick = {
                                viewModel?.qrCodeUseCase?.toScanQrCodePage(this@LoginActivity)
                            },
                            onLoginConfirmButtonClick = { account, password ->
                                viewModel?.loginUseCase?.login(
                                    this@LoginActivity,
                                    account,
                                    password
                                )
                            }
                        )
                    }
                    composable(PageRouteNameProvider.SignUpPage) {
                        SignUpPage(
                            loginState = viewModel?.systemUseCase?.loginSignUpState?.value,
                            userAvatar = viewModel?.systemUseCase?.userAvatarState?.value?.uri,
                            onBackAction = {
                                viewModel?.systemUseCase?.clean()
                                navController.navigateUp()
                            },
                            onSelectUserAvatarClick = {
                                viewModel?.showSelectContentDialog(
                                    this@LoginActivity,
                                    PageRouteNameProvider.SignUpPage
                                )
                            },
                            onConfirmClick = { account: String,
                                               password: String,
                                               userName: String,
                                               userIntroduction: String,
                                               userTags: String,
                                               userSex: String,
                                               userAge: String,
                                               userPhone: String,
                                               userEmail: String,
                                               userArea: String,
                                               userAddress: String,
                                               userWebsite: String ->
                                viewModel?.systemUseCase?.signUp(
                                    this@LoginActivity,
                                    account,
                                    password,
                                    userName,
                                    userIntroduction,
                                    userTags,
                                    userSex,
                                    userAge,
                                    userPhone,
                                    userEmail,
                                    userArea,
                                    userAddress,
                                    userWebsite
                                )
                            }
                        )
                    }
                }
            }
        }
        createObserver()
    }

    override fun createViewModel(): LoginViewModel? {
        return ViewModelProvider(this)[LoginViewModel::class.java]
    }

    fun createObserver() {
        ModuleConstant.MSG_DELIVERY_KEY_USER_LOGIN_ACTION.observeAny2(this, Observer {
            Log.e("LoginActivity", "MSG_DELIVERY_KEY_USER_LOGIN_ACTION")
            finish()
        })
        ModuleConstant.MSG_DELIVERY_KEY_SELECTOR_ITEM_SELECTED.observeAny2(
            this, Observer<Pair<String, List<MediaStoreDataUriWrapper>>?> {
                if (it == null)
                    return@Observer
                viewModel?.dispatchContentSelectedResult(this, it.first, it.second)
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("LoginActivity", "onActivityResult")
        if (requestCode == 9999) {
            if (resultCode == RESULT_OK && data != null) {
                val providerId = data.getStringExtra("providerId") ?: return
                val code = data.getStringExtra("code") ?: return
                viewModel?.qrCodeUseCase?.getQrCodeStatus(providerId, code)
            }
        }
    }

    companion object {
        fun navigate(context: Context) {
            context.startActivity(Intent(context, LoginActivity::class.java))
        }
    }
}

