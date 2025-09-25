package xcj.app.appsets.ui.base

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.annotation.CallStep
import xcj.app.appsets.im.BrokerTest
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.MessageBrokerConstants
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.GroupInfoUseCase
import xcj.app.appsets.usecase.MediaAudioRecorderUseCase
import xcj.app.appsets.usecase.MediaRemoteExoUseCase
import xcj.app.appsets.usecase.NowSpaceContentUseCase
import xcj.app.appsets.usecase.ScreenUseCase
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.usecase.ThirdPartUseCase
import xcj.app.appsets.usecase.UserInfoUseCase
import xcj.app.appsets.util.ktx.asComponentActivityOrNull
import xcj.app.appsets.worker.LastSyncWorker
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel
import xcj.app.io.components.SimpleFileIO
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.AndroidContexts

abstract class BaseIMViewModel : VisibilityComposeStateViewModel() {

    companion object {
        private const val TAG = "BaseIMViewModel"
    }

    val systemUseCase: SystemUseCase = SystemUseCase.getInstance()

    val nowSpaceContentUseCase: NowSpaceContentUseCase = NowSpaceContentUseCase()
    val userInfoUseCase: UserInfoUseCase =
        UserInfoUseCase(
            UserRepository.getInstance(),
            AppSetsRepository.getInstance()
        )
    val groupInfoUseCase: GroupInfoUseCase = GroupInfoUseCase(
        UserRepository.getInstance()
    )
    val screensUseCase: ScreenUseCase = ScreenUseCase(
        ScreenRepository.getInstance()
    )
    val conversationUseCase: ConversationUseCase = ConversationUseCase.getInstance()

    val mediaRemoteExoUseCase: MediaRemoteExoUseCase = MediaRemoteExoUseCase(
        AppSetsRepository.getInstance()
    )
    val mediaAudioRecorderUseCase: MediaAudioRecorderUseCase = MediaAudioRecorderUseCase()

    init {
        conversationUseCase.setNowSpaceContentUseCase(nowSpaceContentUseCase)
    }

    @CallStep(1)
    open fun onActivityCreated(activity: ComponentActivity) {
        PurpleLogger.current.d(TAG, "onActivityCreated")
        doActionsOnCreated()
        mediaRemoteExoUseCase.setLifecycleOwner(activity)
        mediaAudioRecorderUseCase.setLifecycleOwner(activity)
        observeSomeThingsOnCreated(activity)
    }

    @CallStep(2)
    open fun observeSomeThingsOnCreated(activity: ComponentActivity) {
        PurpleLogger.current.d(TAG, "observeSomeThingsOnCreated")

        LocalMessenger.observe<String, ContentSelectionResult>(
            activity,
            ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECT_RESULT
        ) {
            if (it.context.asComponentActivityOrNull() != activity) {
                return@observe
            }
            dispatchContentSelectedResult(activity, it)
        }

        LocalMessenger.observe<String, ImMessage>(
            activity,
            MessageBrokerConstants.MESSAGE_KEY_ON_IM_MESSAGE
        ) {
            PurpleLogger.current.d(TAG, "MESSAGE_KEY_ON_IM_MESSAGE")
        }

        LocalMessenger.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_APP_TOKEN_GOT
        ) {
            doActionsWhenAppTokenGot()
        }

        LocalMessenger.observe<String, Boolean>(
            activity,
            AndroidContexts.MESSAGE_KEY_ON_APP_GO_BACKGROUND
        ) { isAppInBackground ->
            PurpleLogger.current.d(TAG, "isAppInBackground:$isAppInBackground")
            SystemUseCase.startIMServiceIfNeeded(activity, isAppInBackground)
        }

        LocalMessenger.observe<String, Boolean>(
            activity,
            SimpleFileIO.MESSAGE_KEY_ON_COMPONENTS_INITIALED
        ) {
            viewModelScope.launch {
                doActionWhenFileIOInitialed()
            }
        }

        LocalMessenger.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_LOGIN
        ) {
            if (it == LocalAccountManager.LOGIN_BY_NEW) {
                SystemUseCase.startServiceToSyncAllFromServer(activity)
            } else if (it == LocalAccountManager.LOGIN_BY_RESTORE) {
                SystemUseCase.startServiceToSyncAllFromLocal(activity)
            }
        }

        LocalMessenger.observe<String, Boolean>(
            activity,
            LastSyncWorker.MESSAGE_KEY_DATA_SYNC_FINISH
        ) {
            viewModelScope.launch {
                conversationUseCase.initSessionsIfNeeded()
                BrokerTest.start()
            }
        }
    }

    @CallStep(3)
    open fun doActionsOnCreated() {
        PurpleLogger.current.d(TAG, "doActionsOnCreated")
        viewModelScope.launch {
            systemUseCase.initAppToken()
        }
    }

    @CallStep(4)
    private fun doActionsWhenAppTokenGot() {
        PurpleLogger.current.d(TAG, "doActionsWhenAppTokenGot")
        ThirdPartUseCase.newInstance().initSimpleFileIO()
    }

    @CallStep(5)
    open suspend fun doActionWhenFileIOInitialed() {
        PurpleLogger.current.d(TAG, "doActionWhenFileIOInitialed")
        systemUseCase.updateIMBrokerProperties()
        systemUseCase.restoreLoginStatusStateIfNeeded()
    }

    /**
     * 选择内容后
     */
    open fun dispatchContentSelectedResult(
        context: Context,
        contentSelectionResult: ContentSelectionResult
    ) {
        PurpleLogger.current.d(
            TAG,
            "dispatchContentSelectedResult, contentSelectionResults:$contentSelectionResult"
        )
        systemUseCase.selectedContentsStateHolder.updateSelectedContent(contentSelectionResult)
        val type = contentSelectionResult.selectType

        when (type) {
            ContentSelectionTypes.IMAGE -> {
                if (contentSelectionResult !is ContentSelectionResult.RichMediaContentSelectionResult) {
                    return
                }
                val contentUriList = contentSelectionResult.selectItems
                when (contentSelectionResult.request.contextName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val imageUri = contentUriList.firstOrNull()
                        if (imageUri != null) {
                            conversationUseCase.onSendMessage(
                                context,
                                InputSelector.IMAGE,
                                imageUri
                            )
                        }
                    }

                    PageRouteNames.CreateGroupPage -> {
                        val imageUri = contentUriList.firstOrNull()
                        if (imageUri != null) {
                            systemUseCase.updateGroupCreateIconUri(imageUri)
                        }
                    }

                    PageRouteNames.UserProfilePage -> {
                        val imageUri = contentUriList.firstOrNull()
                        if (imageUri != null) {
                            userInfoUseCase.updateUserSelectAvatarUri(imageUri)
                        }
                    }

                    PageRouteNames.SignUpPage -> {
                        val imageUri = contentUriList.firstOrNull()
                        if (imageUri != null) {
                            systemUseCase.updateSignUpUserSelectAvatarUri(imageUri)
                        }
                    }
                }
            }

            ContentSelectionTypes.VIDEO -> {
                if (contentSelectionResult !is ContentSelectionResult.RichMediaContentSelectionResult) {
                    return
                }
                when (contentSelectionResult.request.contextName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val audioUri = contentSelectionResult.selectItems.firstOrNull()
                        if (audioUri != null) {
                            conversationUseCase.onSendMessage(
                                context,
                                InputSelector.VIDEO,
                                audioUri
                            )
                        }
                    }
                }
            }

            ContentSelectionTypes.AUDIO -> {
                if (contentSelectionResult !is ContentSelectionResult.RichMediaContentSelectionResult) {
                    return
                }
                when (contentSelectionResult.request.contextName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val audioUri = contentSelectionResult.selectItems.firstOrNull()
                        if (audioUri != null) {
                            conversationUseCase.onSendMessage(
                                context,
                                InputSelector.MUSIC,
                                audioUri
                            )
                        }
                    }
                }
            }

            ContentSelectionTypes.FILE -> {
                if (contentSelectionResult !is ContentSelectionResult.RichMediaContentSelectionResult) {
                    return
                }
                when (contentSelectionResult.request.contextName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val audioUri = contentSelectionResult.selectItems.firstOrNull()
                        if (audioUri != null) {
                            conversationUseCase.onSendMessage(
                                context,
                                InputSelector.FILE,
                                audioUri
                            )
                        }
                    }
                }
            }

            ContentSelectionTypes.LOCATION -> {
                if (contentSelectionResult !is ContentSelectionResult.LocationContentSelectionResult) {
                    return
                }
                when (contentSelectionResult.request.contextName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        val locationInfo = contentSelectionResult.locationInfo
                        conversationUseCase.onSendMessage(
                            context,
                            InputSelector.LOCATION,
                            locationInfo
                        )
                    }
                }
            }
        }
    }
}