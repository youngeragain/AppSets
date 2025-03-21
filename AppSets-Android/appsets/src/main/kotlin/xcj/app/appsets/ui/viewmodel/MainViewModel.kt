package xcj.app.appsets.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.GenerationAIRepository
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.server.repository.SearchRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.base.BaseIMViewModel
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResults
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionVarargs
import xcj.app.appsets.usecase.AppCreationUseCase
import xcj.app.appsets.usecase.AppsUseCase
import xcj.app.appsets.usecase.NavigationUseCase
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.appsets.usecase.ScreenPostUseCase
import xcj.app.appsets.usecase.SearchUseCase
import xcj.app.compose_share.usecase.ComposeDynamicUseCase
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger

class MainViewModel : BaseIMViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    val composeDynamicUseCase: ComposeDynamicUseCase = ComposeDynamicUseCase(viewModelScope)
    val screenPostUseCase: ScreenPostUseCase = ScreenPostUseCase(
        viewModelScope,
        ScreenRepository.getInstance(),
        GenerationAIRepository.newInstance()
    )
    val searchUseCase: SearchUseCase = SearchUseCase(
        viewModelScope,
        SearchRepository.getInstance()
    )
    val qrCodeUseCase: QRCodeUseCase = QRCodeUseCase(
        viewModelScope,
        systemUseCase.loginSignUpState,
        QRCodeRepository.getInstance(),
        UserRepository.getInstance()
    )
    val appCreationUseCase: AppCreationUseCase = AppCreationUseCase(
        viewModelScope,
        AppSetsRepository.getInstance()
    )

    val navigationUseCase: NavigationUseCase = NavigationUseCase()
    val appsUseCase: AppsUseCase = AppsUseCase(
        viewModelScope,
        AppSetsRepository.getInstance()
    )

    init {
        PurpleLogger.current.d(TAG, "init hash:${hashCode()}")
        conversationUseCase.setNavigationUseCase(navigationUseCase)
    }

    override fun onCleared() {
        PurpleLogger.current.d(TAG, "onCleared")
    }

    override fun doActionWhenFileIOInitialed() {
        super.doActionWhenFileIOInitialed()
        PurpleLogger.current.d(TAG, "doActionWhenFileIOInitialed")
        appsUseCase.loadHomeApplications()
        screensUseCase.loadOutSideScreens()
        systemUseCase.checkUpdate()
    }

    override fun doActionsOnCreated() {
        super.doActionsOnCreated()
        PurpleLogger.current.d(TAG, "doActionsOnCreated")
        systemUseCase.cleanCaches()
    }

    override fun observeSomeThings(activity: ComponentActivity) {
        super.observeSomeThings(activity)

        LocalMessager.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_LOGOUT
        ) {
            nowSpaceContentUseCase.onUserLogout()
            conversationUseCase.onUserLogout()
        }
    }

    override fun handleIntent(intent: Intent) {

    }

    fun dispatchActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        if (data == null) {
            return
        }
        qrCodeUseCase.onActivityResult(context, requestCode, resultCode, data)
        composeDynamicUseCase.onActivityResult(context, requestCode, resultCode, data)
    }

    override fun dispatchContentSelectedResult(
        context: Context,
        contentSelectionResults: ContentSelectionResults
    ) {
        super.dispatchContentSelectedResult(context, contentSelectionResults)
        PurpleLogger.current.d(
            TAG,
            "dispatchContentSelectedResult, contentSelectionResults:$contentSelectionResults"
        )
        systemUseCase.selectedContentsStateHolder.updateSelectedContent(contentSelectionResults)
        val type = contentSelectionResults.selectType

        when (type) {
            ContentSelectionVarargs.PICTURE -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
                val contentUriList = contentSelectionResults.selectItems
                when (contentSelectionResults.contextPageName) {

                    PageRouteNames.CreateScreenPage -> {
                        screenPostUseCase.updateSelectPictures(contentUriList)
                    }


                    PageRouteNames.CreateAppPage -> {
                        val imageUri = contentUriList.firstOrNull()
                        if (imageUri != null) {
                            appCreationUseCase.updateSelectPicture(imageUri)
                        }
                    }
                }
            }

            ContentSelectionVarargs.VIDEO -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
                when (contentSelectionResults.contextPageName) {
                    PageRouteNames.CreateScreenPage -> {
                        screenPostUseCase.updateSelectVideo(
                            contentSelectionResults.selectItems
                        )
                    }
                }
            }

            ContentSelectionVarargs.AUDIO -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
            }

            ContentSelectionVarargs.FILE -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
            }

            ContentSelectionVarargs.LOCATION -> {
                if (contentSelectionResults !is ContentSelectionResults.LocationContentSelectionResults) {
                    return
                }
            }
        }
    }
}