package xcj.app.screen_share.ui.compose

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import xcj.app.screen_share.service.ScreenCaptureService
import xcj.app.starter.android.ui.base.DesignComponentActivity

class MainActivity : DesignComponentActivity() {
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 用户已授权，启动屏幕录制服务
            val data = result.data
            if (data != null) {
                startScreenCaptureService(data)
            } else {
                Toast.makeText(this, "MediaProjection data is null.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 用户拒绝授权
            Toast.makeText(this, "屏幕录制权限被拒绝。", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenShareMainContent(
                onStartCaptureClick = {
                    requestStartCapture()
                },
                onStopCaptureClick = {
                    requestStopCapture()
                }
            )
        }
    }

    private fun requestStopCapture() {
        val stopIntent = Intent(this, ScreenCaptureService::class.java)
        stopIntent.action = ScreenCaptureService.ACTION_STOP
        startService(stopIntent)
        Toast.makeText(this, "停止录制。", Toast.LENGTH_SHORT).show()
    }

    private fun requestStartCapture() {
        if (!::mediaProjectionManager.isInitialized) {
            mediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        }
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        mediaProjectionLauncher.launch(captureIntent) // 使用 Launcher
    }

    // 启动屏幕录制服务
    private fun startScreenCaptureService(data: Intent) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        serviceIntent.action = ScreenCaptureService.ACTION_START
        serviceIntent.putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, RESULT_OK)
        serviceIntent.putExtra(ScreenCaptureService.EXTRA_DATA, data)

        // 启动前台服务
        ContextCompat.startForegroundService(this, serviceIntent)
        Toast.makeText(this, "开始录制...", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ScreenShareMainContent(
    onStartCaptureClick: () -> Unit,
    onStopCaptureClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            Button(onClick = onStartCaptureClick) {
                Text("Start Capture")
            }
            Button(onClick = onStopCaptureClick) {
                Text("Stop Capture")
            }
        }
    }
}