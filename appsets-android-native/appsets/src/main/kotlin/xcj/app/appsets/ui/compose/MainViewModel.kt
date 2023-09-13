package xcj.app.appsets.ui.compose

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.ui.compose.conversation.InputSelector
import xcj.app.appsets.ui.compose.wlanp2p.WLANP2PActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.ui.nonecompose.ui.MediaFallActivity
import xcj.app.appsets.ui.nonecompose.ui.dialog.SelectActionBottomSheetDialog
import xcj.app.appsets.usecase.AppSetsUseCase
import xcj.app.appsets.usecase.BottomMenuUseCase
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.CreateApplicationUseCase
import xcj.app.appsets.usecase.GroupInfoUseCase
import xcj.app.appsets.usecase.MediaUseCase
import xcj.app.appsets.usecase.QrCodeUseCase
import xcj.app.appsets.usecase.ScreenPostUseCase
import xcj.app.appsets.usecase.ScreenUseCase
import xcj.app.appsets.usecase.SearchUseCase
import xcj.app.appsets.usecase.StartUseCase
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.usecase.ThirdPartUseCase
import xcj.app.appsets.usecase.UserInfoUseCase
import xcj.app.appsets.usecase.UserLoginUseCase
import xcj.app.compose_share.compose.usecase.ComposeDynamicUseCase


@UnstableApi
class MainViewModel : BaseViewModel() {
    private val TAG = "MainViewModel"
    var composeDynamicUseCase: ComposeDynamicUseCase? = null
    var systemUseCase: SystemUseCase? = null
    var screenPostUseCase: ScreenPostUseCase? = null
    var userLoginUseCase: UserLoginUseCase? = null
    var searchUseCase: SearchUseCase? = null
    var qrCodeUseCase: QrCodeUseCase? = null
    var createApplicationUseCase: CreateApplicationUseCase? = null
    var groupInfoUseCase: GroupInfoUseCase? = null
    var screensUseCase: ScreenUseCase? = null
    var conversationUseCase: ConversationUseCase? = null

    val mediaUseCase: MediaUseCase = MediaUseCase(arrayOf("remote", "local"), false)
    val bottomMenuUseCase: BottomMenuUseCase = BottomMenuUseCase()
    val appSetsUseCase: AppSetsUseCase = AppSetsUseCase(viewModelScope)
    val startUseCase: StartUseCase = StartUseCase(viewModelScope)
    val userInfoUseCase: UserInfoUseCase = UserInfoUseCase(viewModelScope)



    init {
        Log.i(TAG, "init")
    }

    fun pinApp(appPackageName: String?) {
        startUseCase.addPinnedApp(appPackageName)
    }

    fun unPinApp(appPackageName: String?) {
        startUseCase.unPinApp(appPackageName)
    }

    fun toLoginPageOrSinOut(context: Context) {
        userLoginUseCase?.toLoginPageOrSignOut(context)
    }

    fun onSendMessage(context: Context, inputSelector: InputSelector, content: Any) {
        conversationUseCase?.onSendMessage(context, inputSelector, content)
    }

    fun onReceivedMessage(context: Context, msg: ImMessage, isLocal: Boolean = false) {
        conversationUseCase?.onMessage(context, msg, isLocal)
    }

    fun showSelectContentDialog(context: Context, contextName: String, payload: Any? = null) {
        ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT = contextName
        val selectActionBottomSheetDialog =
            SelectActionBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putBoolean("full_height", true)
                    putStringArray("selected_uri_list", null)
                    if (contextName == PageRouteNameProvider.AddScreenPostPage) {
                        if (payload == "video") {
                            putStringArray("actions", arrayOf("video"))
                            putInt("max_select_count", 1)
                        } else if (payload == "picture") {
                            putStringArray("actions", arrayOf("picture"))
                            putInt("max_select_count", 15)
                        } else {
                            putInt("max_select_count", 1)
                        }
                    } else if (contextName == PageRouteNameProvider.CreateGroup) {
                        putStringArray("actions", arrayOf("picture"))
                        putInt("max_select_count", 1)
                    } else if (contextName == PageRouteNameProvider.CreateAppPage) {
                        putStringArray("actions", arrayOf("picture"))
                        putInt("max_select_count", 1)
                    }

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
                if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.AddScreenPostPage) {
                    screenPostUseCase?.updateSelectPictures(contentUriList)
                } else if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.ConversationDetailsPage) {
                    val imageUri = contentUriList.firstOrNull()
                    if (imageUri != null) {
                        onSendMessage(context, InputSelector.IMAGE, imageUri)
                    }
                } else if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.CreateGroup) {
                    val imageUri = contentUriList.firstOrNull()
                    if (imageUri != null) {
                        systemUseCase?.setUserSelectGroupIconUri(imageUri)
                    }
                } else if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.CreateAppPage) {
                    val imageUri = contentUriList.firstOrNull()
                    if (imageUri != null) {
                        createApplicationUseCase?.updateSelectPicture(imageUri)
                    }
                }
            }

            "video" -> {
                if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.AddScreenPostPage) {
                    val videoUri = contentUriList.firstOrNull()
                    if (videoUri != null)
                        screenPostUseCase?.updateSelectVideo(videoUri)
                }
            }

            "audio" -> {
                if (ModuleConstant.KEY_SELECTOR_ITEM_SELECTED_CONTEXT == PageRouteNameProvider.ConversationDetailsPage) {
                    val audioUri = contentUriList.firstOrNull()
                    if (audioUri != null) {
                        onSendMessage(context, InputSelector.MUSIC, audioUri)
                    }
                }
            }
        }
    }
    private var necessaryActionsExecuted = false
    /**
     * 和用户部分没有关系的数据加载以及初始化部分工具类
     */
    fun doNecessaryActionsWhenAppTokenGot(context: Context) {
        Log.i(TAG, "doNecessaryActionsWhenAppTokenGot")
        if (necessaryActionsExecuted)
            return
        necessaryActionsExecuted = true
        val mEventReceiver = object : EventReceiver {
            override fun getKey(): String {
                return this@MainViewModel.toString()
            }

            override fun onEvent(e: Event) {
                when (e.payload) {
                    "sync_data_finish" -> {
                        conversationUseCase?.initSessions()
                        SystemUseCase.startServiceToStartRabbit(context)
                    }
                }
            }
        }
        EventDispatcher.addEventReceiver(mEventReceiver)
        createNeededUseCase()
        ThirdPartUseCase.getInstance().run {
            setCoroutineScope(viewModelScope)
            initSimpleFileIO(context)
        }
        systemUseCase?.cleanCaches()
        mediaUseCase.onCreate(context)
        appSetsUseCase.checkUpdate(context)
        appSetsUseCase.loadSpotLight()
    }

    fun doNecessaryActionsOnCreate(context: Context) {
        LocalAccountManager.restoreTokenIfNeeded()
    }

    override fun onCleared() {
        super.onCleared()
    }

    private fun createNeededUseCase() {
        if (systemUseCase == null)
            systemUseCase = SystemUseCase(viewModelScope)
        if (userLoginUseCase == null)
            userLoginUseCase = UserLoginUseCase(viewModelScope, systemUseCase!!.loginSignUpState)
        if (qrCodeUseCase == null)
            qrCodeUseCase = QrCodeUseCase(viewModelScope, null)
        if (searchUseCase == null)
            searchUseCase = SearchUseCase(viewModelScope)
        if (screenPostUseCase == null)
            screenPostUseCase = ScreenPostUseCase(viewModelScope)
        if (createApplicationUseCase == null)
            createApplicationUseCase = CreateApplicationUseCase(viewModelScope)
        if (groupInfoUseCase == null)
            groupInfoUseCase = GroupInfoUseCase(viewModelScope)
        if (screensUseCase == null)
            screensUseCase = ScreenUseCase(viewModelScope)
        if (conversationUseCase == null)
            conversationUseCase = ConversationUseCase(viewModelScope)
        if (composeDynamicUseCase == null) {
            composeDynamicUseCase = ComposeDynamicUseCase(viewModelScope)
        }
    }

    fun onUserLogout() {
        conversationUseCase?.clean()
    }

    fun toShareActivity(context: Context) {
        context.startActivity(Intent(context, WLANP2PActivity::class.java))
    }

    fun toMediaFallActivity(context: Context) {
        MediaFallActivity.navigate(context)
    }
}
