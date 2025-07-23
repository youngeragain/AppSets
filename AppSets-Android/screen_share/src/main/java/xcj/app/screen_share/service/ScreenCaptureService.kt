package xcj.app.screen_share.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.getSystemService
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var screenDensityDpi: Int = 0

    private var outputFilePath: String? = null

    companion object {
        const val ACTION_START = "com.your.screencaptureapp.ACTION_START"
        const val ACTION_STOP = "com.your.screencaptureapp.ACTION_STOP"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_DATA = "extra_data"

        private const val NOTIFICATION_CHANNEL_ID = "screen_capture_channel"
        private const val NOTIFICATION_ID = 123
        private const val TAG = "ScreenCaptureService"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // 这是个非绑定服务
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        createNotificationChannel() // 创建通知渠道
        startForeground(NOTIFICATION_ID, createNotification()) // 启动前台服务
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)

                if (resultCode != 0 && data != null) {
                    val mediaProjectionManager =
                        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)

                    // 获取屏幕尺寸和密度
                    val metrics = DisplayMetrics()
                    val windowManager = getSystemService<WindowManager>()
                    windowManager?.defaultDisplay?.getRealMetrics(metrics)
                    screenWidth = metrics.widthPixels
                    screenHeight = metrics.heightPixels
                    screenDensityDpi = metrics.densityDpi

                    startRecording()
                } else {
                    Toast.makeText(this, "无法获取 MediaProjection 权限。", Toast.LENGTH_SHORT)
                        .show()
                    stopSelf() // 无法录制，停止服务
                }
            }

            ACTION_STOP -> {
                stopRecording()
                stopSelf() // 停止服务
            }
        }
        return START_NOT_STICKY // 服务被杀死后不尝试重启
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "屏幕录制服务",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("屏幕录制中")
                .setContentText("您的屏幕正在被录制...")
                .setSmallIcon(android.R.drawable.ic_media_play) // 替换为你的图标
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("屏幕录制中")
                .setContentText("您的屏幕正在被录制...")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }
    }

    private fun startRecording() {
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null. Cannot start recording.")
            Toast.makeText(this, "录制失败：MediaProjection 未准备好。", Toast.LENGTH_SHORT).show()
            stopSelf()
            return
        }

        try {
            // 1. 初始化 MediaRecorder
            mediaRecorder = MediaRecorder().apply {
                // 设置音频源 (如果需要录制音频)
                setAudioSource(MediaRecorder.AudioSource.MIC) // 或 MediaRecorder.AudioSource.DEFAULT
                // 设置视频源
                setVideoSource(MediaRecorder.VideoSource.SURFACE)

                // 设置输出格式
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                // 设置输出文件路径
                outputFilePath = getOutputFilePath()
                setOutputFile(outputFilePath)

                // 设置视频编码器
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                // 设置音频编码器 (如果录制音频)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

                // 设置视频分辨率 (可以根据屏幕尺寸调整)
                setVideoSize(screenWidth, screenHeight)
                // 设置视频帧率
                setVideoFrameRate(30)
                // 设置视频比特率 (根据需求调整，影响文件大小和画质)
                setVideoEncodingBitRate(5 * 1024 * 1024) // 5 Mbps

                // 准备 MediaRecorder
                prepare()
            }

            // 2. 创建 VirtualDisplay
            // 将 MediaRecorder 的输入 Surface 作为 VirtualDisplay 的目标
            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenCapture",
                screenWidth,
                screenHeight,
                screenDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface, // 这里是关键：将 MediaRecorder 的 Surface 传递给 VirtualDisplay
                null,
                null
            )

            // 3. 开始录制
            mediaRecorder?.start()
            Toast.makeText(this, "屏幕录制已开始，文件将保存到：$outputFilePath", Toast.LENGTH_LONG)
                .show()
            Log.d(TAG, "Screen recording started to: $outputFilePath")

        } catch (e: IOException) {
            Log.e(TAG, "Error starting recording: ${e.message}", e)
            Toast.makeText(this, "录制失败：${e.message}", Toast.LENGTH_LONG).show()
            stopRecording() // 出现错误时停止录制并清理
            stopSelf()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Illegal state during recording setup: ${e.message}", e)
            Toast.makeText(this, "录制失败：${e.message}", Toast.LENGTH_LONG).show()
            stopRecording() // 出现错误时停止录制并清理
            stopSelf()
        }
    }

    private fun stopRecording() {
        Log.d(TAG, "Stopping recording...")
        try {
            virtualDisplay?.release()
            virtualDisplay = null

            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null

            mediaProjection?.stop()
            mediaProjection = null

            Toast.makeText(this, "录制已停止，文件保存到：$outputFilePath", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Screen recording stopped.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording: ${e.message}", e)
            Toast.makeText(this, "停止录制时发生错误：${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        stopRecording() // 确保服务销毁时停止录制
    }

    // 获取视频输出文件路径
    private fun getOutputFilePath(): String {
        val moviesDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 (API 29) 及以上，使用 MediaStore API 或应用专属目录
            // 这里为了简单，直接使用外部公共目录，但需要注意权限
            // 更好的方式是使用 MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            getExternalFilesDir(null) // 应用专属目录，无需额外权限
        } else {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        }

        val appDir = File(moviesDir, "ScreenCaptures")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(appDir, "ScreenCapture_$timestamp.mp4").absolutePath
    }
}