package xcj.app.appsets.ui.compose.media.video.fall

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.starter.android.ui.base.DesignFragmentActivity

class MediaFallActivity : DesignFragmentActivity() {

    private val viewModel: MediaFallViewModel by viewModels<MediaFallViewModel>()

    override fun <V : ViewModel> requireViewModel(): V? {
        return viewModel as? V
    }

    override fun isOverrideSystemBarLightModel(): Boolean? {
        return false
    }

    override fun isLayoutInCutOut(): Boolean {
        return true
    }

    override fun isKeepScreenOn(): Boolean {
        return true
    }

    override fun isHideNavigationBar(): Boolean {
        return true
    }

    override fun isHideStatusBar(): Boolean {
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                MediaFallContent()
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                viewModel.onActivityCreated(this@MediaFallActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.detachPlayerView(null)
    }
}