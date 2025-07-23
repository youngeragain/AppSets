package xcj.app.appsets.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.ui.base.BaseIMViewModel
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResults
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.NavigationUseCase
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger

class IMBubbleViewModel : BaseIMViewModel() {
    companion object Companion {
        private const val TAG = "IMBubbleViewModel"
    }

    val navigationUseCase: NavigationUseCase = NavigationUseCase()

    init {
        PurpleLogger.current.d(TAG, "init")
    }

    private var mPendingSessionId: String? = null

    override fun onActivityCreated(activity: ComponentActivity) {
        super.onActivityCreated(activity)
        PurpleLogger.current.d(TAG, "onActivityCreated")
    }

    override fun handleIntent(intent: Intent) {
        PurpleLogger.current.d(TAG, "handleIntent")
        val sessionId = intent.getStringExtra(ImMessage.KEY_SESSION_ID)
        mPendingSessionId = sessionId
        PurpleLogger.current.d(TAG, "handleIntent, sessionId:$sessionId")
        if (!sessionId.isNullOrEmpty()) {
            val notificationId = intent.getIntExtra(ImMessage.KEY_IM_MESSAGE_NOTIFICATION_ID, -1)
            val imMessageId = intent.getStringExtra(ImMessage.KEY_IM_MESSAGE_ID)
            conversationUseCase.updateCurrentSessionBySessionId(sessionId)
        }
    }

    /**
     * 选择内容后
     */
    override fun dispatchContentSelectedResult(
        context: Context,
        contentSelectionResults: ContentSelectionResults
    ) {
        super.dispatchContentSelectedResult(context, contentSelectionResults)
        PurpleLogger.current.d(
            TAG,
            "dispatchContentSelectedResult, contentSelectionResults:$contentSelectionResults"
        )
    }

    override fun observeSomeThings(activity: ComponentActivity) {
        super.observeSomeThings(activity)
        PurpleLogger.current.d(TAG, "observeSomeThings")
        LocalMessager.observe<String, Boolean>(
            activity,
            ConversationUseCase.KEY_SESSIONS_INIT_RESULT
        ) { initSuccess ->
            if (!initSuccess) {
                return@observe
            }
            val pendingSessionId = mPendingSessionId
            if (!pendingSessionId.isNullOrEmpty()) {
                conversationUseCase.updateCurrentSessionBySessionId(pendingSessionId)
            }
        }
    }
}