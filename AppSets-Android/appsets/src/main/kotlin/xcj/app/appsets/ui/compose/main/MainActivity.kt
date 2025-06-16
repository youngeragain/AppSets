package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.MotionEvent
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.notification.NotificationPusher
import xcj.app.appsets.ui.compose.LocalPageRouteNameNeedLoggedProvider
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.SplashScreenHelper
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PurpleLogger
import java.util.UUID

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
            lifecycle.withCreated {
                createBroadcastReceiver()
                viewModel.onActivityCreated(this@MainActivity)
                viewModel.handleIntent(intent)

                Handler(Looper.getMainLooper()).post {
                    lifecycleScope.launch {
                        handleExternalShareContentIfNeeded(intent)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleIntent(intent)
        lifecycleScope.launch {
            handleExternalShareContentIfNeeded(intent)
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun createBroadcastReceiver() {
        PurpleLogger.current.d(TAG, "createBroadcastReceiver")
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

    override fun onDestroy() {
        super.onDestroy()
        if (::mImMessageNotificationIntentReceiver.isInitialized) {
            unregisterReceiver(mImMessageNotificationIntentReceiver)
        }
    }

    private suspend fun handleExternalShareContentIfNeeded(intent: Intent?) {
        if (intent == null) {
            return
        }
        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                handleIntentExternalContent(intent)
            }
        }
    }

    private suspend fun handleIntentExternalContent(
        intent: Intent
    ) {
        delay(150)
        val fromAppDefinition = getCallActivityAppDefinition()
        val bottomSheetState = viewModel.bottomSheetState()
        bottomSheetState.setShouldBackgroundSink(true)
        bottomSheetState.show {
            ExternalContentContainerSheet(
                intent = intent,
                fromAppDefinition = fromAppDefinition,
                onConfirmClick = { handleType ->
                    when (handleType) {
                        EXTERNAL_CONTENT_HANDLE_BY_LOCAL_SHARE -> {
                            bottomSheetState.hide()
                            handleExternalDataByAppSetsShare(intent)

                        }

                        EXTERNAL_CONTENT_HANDLE_BY_APPSETS -> {
                        }
                    }
                }
            )
        }
    }

    private fun handleExternalDataByAppSetsShare(intent: Intent) {
        navigateToAppSetsShareActivity(this, intent)
    }

    private suspend fun getCallActivityAppDefinition(): AppDefinition? {
        val callingPackage = getCallingPackage()
        return if (callingPackage != null) {
            getAppNameFromPackageName(callingPackage)
        } else {
            null
        }
    }

    private suspend fun getAppNameFromPackageName(packageName: String): AppDefinition? =
        withContext(
            Dispatchers.IO
        ) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appDefinition = AppDefinition(UUID.randomUUID().toString())
                appDefinition.applicationInfo = appInfo
                appDefinition.name = appInfo.loadLabel(packageManager).toString().trim()
                appDefinition.icon = appInfo.loadIcon(packageManager)
                return@withContext appDefinition
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return@withContext null
            }
        }

    override fun onEnterAnimationComplete() {
        PurpleLogger.current.d(TAG, "onEnterAnimationComplete")
    }
}