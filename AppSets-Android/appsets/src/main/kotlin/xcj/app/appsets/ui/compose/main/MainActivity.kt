package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.notification.NotificationPusher
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.SplashScreenHelper
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PackageUtil
import xcj.app.starter.android.util.PurpleLogger

class MainActivity : DesignComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTERNAL_CONTENT_HANDLE_BY_APPSETS = 0
        const val EXTERNAL_CONTENT_HANDLE_BY_LOCAL_SHARE = 1
    }

    private lateinit var mImMessageNotificationIntentReceiver: BroadcastReceiver

    private val viewModel by viewModels<MainViewModel>()

    override fun requireViewModel(): MainViewModel {
        return viewModel
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SplashScreenHelper.onActivityCreate(this)
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                MainPages()
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                listenBroadcast()
                viewModel.onActivityCreated(this@MainActivity)
            }
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.handleIntent(intent)
                handleExternalShareContentIfNeeded(intent)
            }
            lifecycle.repeatOnLifecycle(Lifecycle.State.DESTROYED) {
                unListenBroadcast()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleIntent(intent)
        handleExternalShareContentIfNeeded(intent)
    }

    private fun unListenBroadcast() {
        PurpleLogger.current.d(TAG, "unListenBroadcast")
        if (::mImMessageNotificationIntentReceiver.isInitialized) {
            unregisterReceiver(mImMessageNotificationIntentReceiver)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun listenBroadcast() {
        PurpleLogger.current.d(TAG, "listenBroadcast")
        if (::mImMessageNotificationIntentReceiver.isInitialized) {
            return
        }
        mImMessageNotificationIntentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                PurpleLogger.current.d(TAG, "BroadcastReceiver, onReceive")
                viewModel.conversationUseCase.handleSystemNotificationForReplyImMessage(
                    this@MainActivity,
                    intent
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                mImMessageNotificationIntentReceiver,
                IntentFilter(NotificationPusher.ACTION_RECEIVER_IM_SESSION_REPLY),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                mImMessageNotificationIntentReceiver,
                IntentFilter(NotificationPusher.ACTION_RECEIVER_IM_SESSION_REPLY)
            )
        }
    }


    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        val onBackInvokedDispatcher = super.getOnBackInvokedDispatcher()
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             val t: OnBackInvokedCallback = object :OnBackInvokedCallback{
                 override fun onBackInvoked() {
                     PurpleLogger.current.d(TAG, "back gesture invoke")
                 }
             }
             onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, t)
         }*/
        return onBackInvokedDispatcher
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.dispatchActivityResult(this, requestCode, resultCode, data)

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    private fun handleExternalShareContentIfNeeded(intent: Intent?) {
        if (intent == null) {
            return
        }
        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                handleIntentExternalContent(intent)
            }
        }
    }

    private fun handleIntentExternalContent(
        intent: Intent,
    ) {
        //wait compose first frame draw finish
        lifecycleScope.launch {
            delay(120)
            val fromAppDefinition = getCallActivityAppDefinition()
            val composeContainerState = viewModel.bottomSheetState()
            composeContainerState.setShouldBackgroundSink(true)
            composeContainerState.show {
                ExternalContentSheetContent(
                    intent = intent,
                    fromAppDefinition = fromAppDefinition,
                    onConfirmClick = { handleType ->
                        when (handleType) {
                            EXTERNAL_CONTENT_HANDLE_BY_LOCAL_SHARE -> {
                                composeContainerState.hide()
                                handleExternalDataByAppSetsShare(intent)

                            }

                            EXTERNAL_CONTENT_HANDLE_BY_APPSETS -> {
                            }
                        }
                    }
                )
            }
        }
    }

    private fun handleExternalDataByAppSetsShare(intent: Intent) {
        navigateToAppSetsShareActivity(this, intent)
    }

    private suspend fun getCallActivityAppDefinition(): AppDefinition? {
        val callingPackage = getCallingPackage()
        if (callingPackage.isNullOrEmpty()) {
            return null
        }
        return PackageUtil.getAppDefinitionByPackageName(this, callingPackage)
    }


    override fun onEnterAnimationComplete() {
        PurpleLogger.current.d(TAG, "onEnterAnimationComplete")
    }
}