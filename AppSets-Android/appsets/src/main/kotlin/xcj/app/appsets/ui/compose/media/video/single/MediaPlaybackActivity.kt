package xcj.app.appsets.ui.compose.media.video.single

import android.content.res.Configuration
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
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PurpleLogger

class MediaPlaybackActivity : DesignComponentActivity() {

    companion object {
        private const val TAG = "MediaPlaybackActivity"
        const val KEY_VIDEO_JSON_DATA = "video_json"
    }

    private val viewModel: MediaPlaybackViewModel by viewModels<MediaPlaybackViewModel>()

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
        PurpleLogger.current.d(TAG, "onCreate")
        setContent {
            AppSetsTheme {
                MediaPlaybackContent()
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                viewModel.onActivityCreated(this@MediaPlaybackActivity)
                viewModel.handleIntent(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.detachPlayerView(null)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        PurpleLogger.current.d(TAG, "onConfigurationChanged")
    }
}
