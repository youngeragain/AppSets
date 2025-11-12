package xcj.app.appsets.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.GenerationAIRepository
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.server.repository.SearchRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.base.BaseIMViewModel
import xcj.app.appsets.usecase.AppCreationUseCase
import xcj.app.appsets.usecase.AppsUseCase
import xcj.app.appsets.usecase.NavigationUseCase
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.appsets.usecase.ScreenPostUseCase
import xcj.app.appsets.usecase.SearchUseCase
import xcj.app.compose_share.usecase.ComposeDynamicUseCase
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger

class MainViewModel : BaseIMViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    val composeDynamicUseCase: ComposeDynamicUseCase = ComposeDynamicUseCase()
    val screenPostUseCase: ScreenPostUseCase = ScreenPostUseCase(
        ScreenRepository.getInstance(),
        GenerationAIRepository.newInstance()
    )
    val searchUseCase: SearchUseCase = SearchUseCase(
        SearchRepository.getInstance()
    )
    val qrCodeUseCase: QRCodeUseCase = QRCodeUseCase(
        QRCodeRepository.getInstance(),
        UserRepository.getInstance()
    )
    val appCreationUseCase: AppCreationUseCase = AppCreationUseCase(
        AppSetsRepository.getInstance()
    )

    val navigationUseCase: NavigationUseCase = NavigationUseCase()
    val appsUseCase: AppsUseCase = AppsUseCase(
        AppSetsRepository.getInstance()
    )

    init {
        PurpleLogger.current.d(TAG, "init hash:${hashCode()}")
    }

    override fun onCleared() {
        PurpleLogger.current.d(TAG, "onCleared")
    }

    override fun doActionsOnCreated() {
        super.doActionsOnCreated()
        PurpleLogger.current.d(TAG, "doActionsOnCreated")
        systemUseCase.cleanCaches()
    }

    override suspend fun doActionWhenFileIOInitialed(context: Context) {
        super.doActionWhenFileIOInitialed(context)
        PurpleLogger.current.d(TAG, "doActionWhenFileIOInitialed")
        appsUseCase.loadHomeApplications()
        screensUseCase.loadOutSideScreens()
        systemUseCase.showPlatformPermissionUsageTipsIfNeeded(
            nowSpaceContentUseCase = nowSpaceContentUseCase,
            showFlow = AppSetsModuleSettings.get().isAppFirstLaunch()
        )
        systemUseCase.checkUpdate(nowSpaceContentUseCase)

    }

    override fun observeSomeThingsOnCreated(activity: ComponentActivity) {
        super.observeSomeThingsOnCreated(activity)

        LocalMessenger.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_LOGOUT
        ) { by ->
            nowSpaceContentUseCase.onUserLogout(by)
            conversationUseCase.onUserLogout(by)
        }
    }

    override fun handleIntent(intent: Intent) {

    }

    fun dispatchActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        viewModelScope.launch {
            qrCodeUseCase.onActivityResult(context, requestCode, resultCode, data)
            composeDynamicUseCase.onActivityResult(context, requestCode, resultCode, data)
        }
    }
}