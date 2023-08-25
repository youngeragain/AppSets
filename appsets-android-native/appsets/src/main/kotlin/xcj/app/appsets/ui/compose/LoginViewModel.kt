package xcj.app.appsets.ui.compose

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.ui.nonecompose.ui.dialog.SelectActionBottomSheetDialog
import xcj.app.appsets.usecase.QrCodeUseCase
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.usecase.UserLoginUseCase
import xcj.app.purple_module.ModuleConstant


class LoginViewModel : BaseViewModel() {
    val systemUseCase: SystemUseCase = SystemUseCase(viewModelScope)
    val loginUseCase: UserLoginUseCase =
        UserLoginUseCase(viewModelScope, systemUseCase.loginSignUpState)
    val qrCodeUseCase: QrCodeUseCase = QrCodeUseCase(viewModelScope, systemUseCase.loginSignUpState)


    fun login(context: Context, account: String, password: String) {
        loginUseCase.login(context, account, password)
    }


    fun showSelectContentDialog(context: Context, contextName: String) {
        ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT = contextName
        val selectActionBottomSheetDialog =
            SelectActionBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putBoolean("full_height", true)
                    putInt("max_select_count", 1)
                    putStringArray("actions", arrayOf("picture"))
                }
            }

        selectActionBottomSheetDialog.show(
            (context as FragmentActivity).supportFragmentManager,
            selectActionBottomSheetDialog.tag
        )
    }


    /**
     * 选择文件或视频或图片后
     */
    fun dispatchContentSelectedResult(
        context: Context,
        type: String,
        contentUriList: List<MediaStoreDataUriWrapper>
    ) {
        when (type) {
            "picture" -> {
                if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.SignUpPage) {
                    val imageUri = contentUriList.firstOrNull()
                    if (imageUri != null) {
                        systemUseCase.setUserSelectAvatarUri(imageUri)
                    }
                }
            }
        }
    }


}

