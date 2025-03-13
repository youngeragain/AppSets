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
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResults
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionVarargs
import xcj.app.appsets.usecase.ActivityLifecycleUseCase
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
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel
import xcj.app.io.components.SimpleFileIO
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger

abstract class BaseIMViewModel : AnyStateViewModel() {

    companion object {
        private const val TAG = "BaseIMViewModel"
    }

    val systemUseCase: SystemUseCase = SystemUseCase.getInstance()

    val nowSpaceContentUseCase: NowSpaceContentUseCase = NowSpaceContentUseCase.getInstance()
    val activityLifecycleUseCase: ActivityLifecycleUseCase = ActivityLifecycleUseCase()
    val userInfoUseCase: UserInfoUseCase =
        UserInfoUseCase(
            viewModelScope,
            UserRepository.getInstance(),
            AppSetsRepository.getInstance()
        )
    val groupInfoUseCase: GroupInfoUseCase = GroupInfoUseCase(
        viewModelScope,
        UserRepository.getInstance()
    )
    val screensUseCase: ScreenUseCase = ScreenUseCase(
        viewModelScope,
        ScreenRepository.getInstance()
    )
    val conversationUseCase: ConversationUseCase = ConversationUseCase.getInstance()
    //val mediaLocalExoUseCase: MediaLocalExoUseCase = MediaLocalExoUseCase()

    val mediaRemoteExoUseCase: MediaRemoteExoUseCase = MediaRemoteExoUseCase(
        viewModelScope,
        AppSetsRepository.getInstance()
    )
    val mediaAudioRecorderUseCase: MediaAudioRecorderUseCase = MediaAudioRecorderUseCase(
        viewModelScope
    )

    @CallStep(1)
    open fun onActivityCreated(activity: ComponentActivity) {
        PurpleLogger.current.d(TAG, "onActivityCreated")
        doActionsOnCreated()
        activityLifecycleUseCase.setLifecycleOwner(activity)
        mediaRemoteExoUseCase.setLifecycleOwner(activity)
        mediaAudioRecorderUseCase.setLifecycleOwner(activity)
        observeSomeThings(activity)
    }

    @CallStep(2)
    open fun observeSomeThings(activity: ComponentActivity) {
        PurpleLogger.current.d(TAG, "observeSomeThings")

        LocalMessager.observe<String, ContentSelectionResults>(
            activity,
            ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECT_RESULT
        ) {
            if (it.context.asComponentActivityOrNull() != activity) {
                return@observe
            }
            dispatchContentSelectedResult(activity, it)
        }

        LocalMessager.observe<String, ImMessage>(
            activity,
            MessageBrokerConstants.MESSAGE_KEY_ON_IM_MESSAGE
        ) {
            PurpleLogger.current.d(TAG, "MESSAGE_KEY_ON_IM_MESSAGE")
        }

        LocalMessager.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_APP_TOKEN_GOT
        ) {
            doActionsWhenAppTokenGot()
        }

        LocalMessager.observe<String, Boolean>(
            activity,
            SimpleFileIO.MESSAGE_KEY_ON_COMPONENTS_INITIALED
        ) {
            doActionWhenFileIOInitialed()
        }

        LocalMessager.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_LOGIN
        ) {
            if (it == LocalAccountManager.LOGIN_BY_NEW) {
                SystemUseCase.startServiceToSyncAllFromServer(activity)
            } else if (it == LocalAccountManager.LOGIN_BY_RESTORE) {
                SystemUseCase.startServiceToSyncAllFromLocal(activity)
            }
        }

        LocalMessager.observe<String, Boolean>(
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
    }

    @CallStep(4)
    private fun doActionsWhenAppTokenGot() {
        PurpleLogger.current.d(TAG, "doActionsWhenAppTokenGot")
        ThirdPartUseCase.newInstance().initSimpleFileIO()
    }

    @CallStep(5)
    open fun doActionWhenFileIOInitialed() {
        PurpleLogger.current.d(TAG, "doActionWhenFileIOInitialed")
        systemUseCase.updateIMBrokerProperties()
        systemUseCase.restoreLoginStatusStateIfNeeded()
    }

    /**
     * 选择内容后
     */
    open fun dispatchContentSelectedResult(
        context: Context,
        contentSelectionResults: ContentSelectionResults
    ) {
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

            ContentSelectionVarargs.VIDEO -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
                when (contentSelectionResults.contextPageName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val audioUri = contentSelectionResults.selectItems.firstOrNull()
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

            ContentSelectionVarargs.AUDIO -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
                when (contentSelectionResults.contextPageName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val audioUri = contentSelectionResults.selectItems.firstOrNull()
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

            ContentSelectionVarargs.FILE -> {
                if (contentSelectionResults !is ContentSelectionResults.RichMediaContentSelectionResults) {
                    return
                }
                when (contentSelectionResults.contextPageName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        //todo multi
                        val audioUri = contentSelectionResults.selectItems.firstOrNull()
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

            ContentSelectionVarargs.LOCATION -> {
                if (contentSelectionResults !is ContentSelectionResults.LocationContentSelectionResults) {
                    return
                }
                when (contentSelectionResults.contextPageName) {
                    PageRouteNames.ConversationDetailsPage -> {
                        val locationInfo = contentSelectionResults.locationInfo
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