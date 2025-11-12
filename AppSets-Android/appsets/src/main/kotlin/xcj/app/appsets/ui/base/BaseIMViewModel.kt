package xcj.app.appsets.ui.base

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.annotation.CallStep
import xcj.app.appsets.im.BrokerTest
import xcj.app.appsets.im.MessageBrokerConstants
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionRequest
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
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

        LocalMessenger.observe<String, ContentSelectionRequest>(
            activity,
            ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECTION_CLOSE
        ) { contentSelectionRequest ->
            if (contentSelectionRequest.context.asComponentActivityOrNull() != activity) {
                return@observe
            }
            onContentSelectSheetClosed(activity, contentSelectionRequest)
        }

        LocalMessenger.observe<String, ContentSelectionRequest>(
            activity,
            ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECT_REQUEST
        ) { contentSelectionRequest ->
            if (contentSelectionRequest.context.asComponentActivityOrNull() != activity) {
                return@observe
            }
            onContentSelectionRequest(activity, contentSelectionRequest)
        }

        LocalMessenger.observe<String, ContentSelectionResult<*>>(
            activity,
            ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECT_RESULT
        ) { contentSelectionResult ->
            if (contentSelectionResult.context.asComponentActivityOrNull() != activity) {
                return@observe
            }
            onContentSelectionResult(activity, contentSelectionResult)
        }

        LocalMessenger.observe<String, IMMessage<*>>(
            activity,
            MessageBrokerConstants.MESSAGE_KEY_ON_IM_MESSAGE
        ) { iMMessage ->
            PurpleLogger.current.d(TAG, "MESSAGE_KEY_ON_IM_MESSAGE, iMMessage:$iMMessage")
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
                doActionWhenFileIOInitialed(activity)
            }
        }

        LocalMessenger.observe<String, String>(
            activity,
            LocalAccountManager.MESSAGE_KEY_ON_LOGIN
        ) { loginBy ->
            if (loginBy == LocalAccountManager.LOGIN_BY_NEW) {
                SystemUseCase.startServiceToSyncAllFromServer(activity)
            } else if (loginBy == LocalAccountManager.LOGIN_BY_RESTORE) {
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
    open suspend fun doActionWhenFileIOInitialed(context: Context) {
        PurpleLogger.current.d(TAG, "doActionWhenFileIOInitialed")
        systemUseCase.updateIMBrokerProperties()
        systemUseCase.restoreLoginStatusStateIfNeeded()
    }

    open fun onContentSelectSheetClosed(
        activity: ComponentActivity,
        contentSelectionRequest: ContentSelectionRequest
    ) {

    }

    /**
     * 请求选择内容时
     */
    open fun onContentSelectionRequest(
        context: Context,
        contentSelectionRequest: ContentSelectionRequest
    ) {

    }

    /**
     * 选择内容后
     */
    open fun onContentSelectionResult(
        context: Context,
        contentSelectionResult: ContentSelectionResult<*>
    ) {

    }
}