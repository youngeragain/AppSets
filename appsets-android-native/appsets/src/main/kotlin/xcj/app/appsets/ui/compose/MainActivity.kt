package xcj.app.appsets.ui.compose

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.RabbitMqBroker
import xcj.app.appsets.im.Session
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.observeAny2
import xcj.app.appsets.ui.compose.conversation.InputSelector
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.nonecompose.base.BaseActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.util.NotificationPusher
import xcj.app.core.android.annotations.EntryPoint
import xcj.app.core.android.annotations.PageHelper
import xcj.app.core.test.Purple
import xcj.app.core.test.SimplePurpleForAndroidContext
import xcj.app.purple_module.ModuleConstant

@UnstableApi
@EntryPoint("/main_page")
@PageHelper(MainViewModel::class, ViewDataBinding::class, BaseViewModelFactory::class, "")
class MainActivity :
    BaseActivity<ViewDataBinding, MainViewModel, BaseViewModelFactory<MainViewModel>>() {
    private val TAG = "MainActivity"
    private lateinit var mNotificationManagerCompat: NotificationManagerCompat

    override fun createViewModel(): MainViewModel? {
        val c = 0
        return ViewModelProvider(this)[MainViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                MainContainerCompose()
            }
        }
        createObserver()
        viewModel?.doNecessaryActionsOnCreate(this)
    }

    @SuppressLint("MissingPermission")
    fun toReplyImMessage(intent: Intent) {
        if (viewModel == null)
            return
        if (viewModel!!.conversationUseCase == null)
            return
        val sessionId = intent.getStringExtra("sessionId")!!
        val session: Session = viewModel!!.conversationUseCase!!.getSessionBySessionId(sessionId)
        viewModel!!.conversationUseCase?.updateCurrentSessionBySession(session)
        val userInputContentInNotification =
            RemoteInput.getResultsFromIntent(intent)?.getCharSequence("key_text_reply") ?: ""
        mNotificationManagerCompat.cancel(intent.getIntExtra("imMessageNotificationId", -1))
        viewModel!!.conversationUseCase?.onSendMessage(
            this,
            InputSelector.TEXT,
            userInputContentInNotification
        )
    }

    private lateinit var mImMessageNotificationIntentReceiver: BroadcastReceiver


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun createObserver() {
        if (!::mImMessageNotificationIntentReceiver.isInitialized) {
            mImMessageNotificationIntentReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {
                    Log.e(TAG, "BroadcastReceiver, onReceive")
                    toReplyImMessage(intent)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    mImMessageNotificationIntentReceiver,
                    IntentFilter("xcj.app.conversation.session.im.session_reply"),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                registerReceiver(
                    mImMessageNotificationIntentReceiver,
                    IntentFilter("xcj.app.conversation.session.im.session_reply")
                )
            }
        }

        Log.e(TAG, "createObserver when onCreate")
        ModuleConstant.MSG_DELIVERY_KEY_USER_LOGIN_ACTION.observeAny2(this, Observer {
            Log.e(TAG, "USER_LOGIN_ACTION by:${it}")
            if (it == "by_new_status") {
                SystemUseCase.startServiceToSyncAllFromServer(this)
            } else if (it == "by_restore_status") {
                SystemUseCase.startServiceToSyncAllFromLocal(this)
            }
        })
        ModuleConstant.MSG_DELIVERY_KEY_USER_LOGOUT_ACTION.observeAny2(this, Observer {
            Log.e(TAG, "USER_LOGOUT_ACTION observe")
            viewModel?.onUserLogout()
        })

        RabbitMqBroker.ON_MESSAGE_KEY.observeAny2(this, Observer<ImMessage?> {
            if (it == null)
                return@Observer
            viewModel?.onReceivedMessage(this, it, false)
        })


        ModuleConstant.MSG_DELIVERY_KEY_SELECTOR_ITEM_SELECTED.observeAny2(
            this, Observer<Pair<String, List<MediaStoreDataUriWrapper>>?> {
                if (it == null)
                    return@Observer
                viewModel?.dispatchContentSelectedResult(this, it.first, it.second)
            })


        viewModel?.run {
            win11SnapShotUseCase.pinnedAppPackageNames.observe(this@MainActivity) {
                win11SnapShotUseCase.getPinnedApps()
            }
            appSetsUseCase.appTokenInitialized.observe(this@MainActivity) {
                doNecessaryActionsWhenAppTokenGot(this@MainActivity)
            }
        }
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        val onBackInvokedDispatcher = super.getOnBackInvokedDispatcher()
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             val t: OnBackInvokedCallback = object :OnBackInvokedCallback{
                 override fun onBackInvoked() {
                     Log.e("blue", "back gesture invoke")
                 }
             }
             onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, t)
         }*/
        return onBackInvokedDispatcher
    }

    override fun onStart() {
        super.onStart()
        viewModel?.mediaUseCase?.onStart(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel?.mediaUseCase?.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel?.mediaUseCase?.onPause(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel?.mediaUseCase?.onStop(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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

    override fun pushNotificationIfNeeded(
        notificationPusher: NotificationPusher,
        sessionId: String,
        imMessage: ImMessage
    ) {
        if (lifecycle.currentState == Lifecycle.State.DESTROYED) {
            return
        }
        val simplePurpleForAndroidContext =
            Purple.getPurpleContext() as SimplePurpleForAndroidContext
        if (!simplePurpleForAndroidContext.androidContexts.isApplicationInBackground()) {
            return
        }
        if (!::mNotificationManagerCompat.isInitialized)
            mNotificationManagerCompat = NotificationManagerCompat.from(this)
        notificationPusher.pushConversionNotification(
            this,
            mNotificationManagerCompat,
            sessionId,
            imMessage
        )
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (viewModel == null)
            return
    }

    companion object {
        fun navigate(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}


