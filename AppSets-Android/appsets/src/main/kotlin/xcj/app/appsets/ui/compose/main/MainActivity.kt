package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.notification.NotificationPusher
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.QuickStepSheet
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.ui.compose.quickstep.UriQuickStepContent
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.SplashScreenHelper
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType

class MainActivity : DesignComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
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
                lifecycleScope.launch {
                    viewModel.handleIntent(intent)
                    handleExternalShareContentIfNeeded(intent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            viewModel.handleIntent(intent)
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

    suspend fun handleExternalShareContentIfNeeded(intent: Intent?) {
        if (intent == null) {
            return
        }
        when (intent.action) {
            Intent.ACTION_SEND -> {
                if (ContentType.TEXT_PLAIN == intent.type) {
                    handleIntentExternalContent(intent, true, false)
                } else {
                    handleIntentExternalContent(intent, false, false)
                }
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                handleIntentExternalContent(intent, false, true)
            }
        }
    }

    private suspend fun handleIntentExternalContent(
        intent: Intent, isText: Boolean, isMulti: Boolean
    ) {
        val quickStepContentList = mutableListOf<QuickStepContent>()
        if (isText) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                val textQuickStepContent = TextQuickStepContent(text)
                quickStepContentList.add(textQuickStepContent)
            }
        } else {
            if (!isMulti) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
                    val textQuickStepContent = UriQuickStepContent(uri)
                    quickStepContentList.add(textQuickStepContent)
                }
            } else {
                intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
                    ?.mapNotNull {
                        it as? Uri
                    }?.forEach { uri ->
                        val textQuickStepContent = UriQuickStepContent(uri)
                        quickStepContentList.add(textQuickStepContent)
                    }
            }
        }
        //remove this
        delay(200)
        var bottomSheetState = viewModel.bottomSheetState()
        bottomSheetState.show {
            QuickStepSheet(quickStepContentList)
        }
    }
}