package xcj.app.appsets.ui.compose.camera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.starter.android.ui.base.DesignComponentActivity

class DesignCameraActivity : DesignComponentActivity() {
    companion object {
        const val REQUEST_CODE = 9999
        private const val TAG = "CameraComposeActivity"
    }
    private val viewModel by viewModels<DesignCameraViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                CameraPage(
                    onBackClick = {
                        this.onBackPressedDispatcher.onBackPressed()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}