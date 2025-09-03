package xcj.app.appsets.ui.compose.conversation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.viewmodel.IMBubbleViewModel
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PurpleLogger

class IMBubbleActivity : DesignComponentActivity() {
    companion object Companion {
        private const val TAG = "IMBubbleActivity"
    }

    private val viewModel by viewModels<IMBubbleViewModel>()

    override fun requireViewModel(): IMBubbleViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                ImBubblePages()
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                viewModel.onActivityCreated(this@IMBubbleActivity)
                viewModel.handleIntent(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        PurpleLogger.current.d(TAG, "onNewIntent")
        viewModel.handleIntent(intent)
    }
}