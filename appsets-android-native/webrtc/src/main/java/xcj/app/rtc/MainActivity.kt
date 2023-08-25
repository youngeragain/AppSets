package xcj.app.rtc

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.*
import org.webrtc.SurfaceTextureHelper.FrameRefMonitor
import xcj.app.rtc.ui.theme.AndroidProjectsForXcjTheme
import java.util.concurrent.Callable

class MainViewModel:ViewModel(){

}

class MainActivity : ComponentActivity() {
    private val mViewModel:MainViewModel by viewModels()
    lateinit var eglBase:EglBase
    lateinit var peerConnectionFactory:PeerConnectionFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eglBase = EglBase.create()
        val options =
            PeerConnectionFactory.InitializationOptions
                .builder(this@MainActivity.applicationContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory()
        setContent {
            AndroidProjectsForXcjTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    fun createCameraCapture(context: Context):VideoCapturer? {
        val camera2Enumerator = Camera2Enumerator(context.applicationContext)
        val deviceNames = camera2Enumerator.deviceNames
        return deviceNames.firstOrNull { deviceName->
            camera2Enumerator.isBackFacing(deviceName)
        }?.let { backFacingName->
            camera2Enumerator.createCapturer(backFacingName, object:CameraVideoCapturer.CameraEventsHandler{
                override fun onCameraError(p0: String?) {
                        Log.e("createCapturer", "onCameraError:${p0}")
                }

                override fun onCameraDisconnected() {
                    Log.e("createCapturer", "onCameraDisconnected")
                }

                override fun onCameraFreezed(p0: String?) {
                    Log.e("createCapturer", "onCameraFreezed:${p0}")
                }

                override fun onCameraOpening(p0: String?) {
                    Log.e("createCapturer", "onCameraOpening:${p0}")
                }

                override fun onFirstFrameAvailable() {
                    Log.e("createCapturer", "onFirstFrameAvailable")
                }

                override fun onCameraClosed() {
                    Log.e("createCapturer", "onCameraClosed")
                }
            })
        }
    }
}

@Composable
fun MainScreen() {
    Box {
        AndroidView(
            factory = ::SurfaceViewRenderer,
            modifier = Modifier.fillMaxSize(),
            update = { render->
                Log.e("blue", "othersCameraView")
                render.background = ColorDrawable(Color.GRAY)
            })
        AndroidView(
            factory = ::SurfaceViewRenderer,
            modifier = Modifier
                .size(180.dp, 300.dp)
                .padding(start = 12.dp, top = 12.dp),
            update = { render->
                Log.e("blue", "mineCameraView")
                render.setMirror(false)
                val mainActivity = render.context as MainActivity
                mainActivity.lifecycleScope.launch(Dispatchers.IO) {
                    mainActivity.createCameraCapture(mainActivity)?.let { backCamera->
                        val audioSource = mainActivity.peerConnectionFactory.createAudioSource(MediaConstraints())
                        val videoSource = mainActivity.peerConnectionFactory.createVideoSource(backCamera.isScreencast)
                        val surfaceTextureHelper2 = SurfaceTextureHelper2.create(mainActivity, "camera_capture_thread", mainActivity.eglBase.eglBaseContext)
                        backCamera.initialize(surfaceTextureHelper2, mainActivity.applicationContext, videoSource.capturerObserver)
                        backCamera.startCapture(render.measuredWidth, render.measuredHeight, 30)
                        withContext(Dispatchers.Main){
                            render.init(mainActivity.eglBase.eglBaseContext, object :RendererCommon.RendererEvents{
                                override fun onFirstFrameRendered() {

                                }

                                override fun onFrameResolutionChanged(p0: Int, p1: Int, p2: Int) {

                                }
                            })
                        }
                        val audioTrack = mainActivity.peerConnectionFactory.createAudioTrack("1", audioSource)
                        val videoTrack = mainActivity.peerConnectionFactory.createVideoTrack("2", videoSource)
                        videoTrack.addSink(render)
                    }
                }
            })

    }
}

object SurfaceTextureHelper2{
    @JvmStatic
    fun create(
        context: Context,
        threadName: String,
        sharedContext: EglBase.Context?,
        alignTimestamps: Boolean,
        yuvConverter: YuvConverter?,
        frameRefMonitor: FrameRefMonitor?
    ): SurfaceTextureHelper? {
        val handlerThread = HandlerThread(threadName)
        handlerThread.setUncaughtExceptionHandler { thread, throwable ->
            if(throwable.message?.contains("without camera permission") == true){
                //it can be toast, because this thread has a looper!
                Toast.makeText(context, "without camera permission", Toast.LENGTH_SHORT).show()
            }

        }
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        return ThreadUtils.invokeAtFrontUninterruptibly(handler, Callable {
            kotlin.runCatching {
                val helperConstructor =
                    SurfaceTextureHelper::class.java.getDeclaredConstructor(
                        EglBase.Context::class.java,
                        Handler::class.java,
                        Boolean::class.java,
                        YuvConverter::class.java,
                        FrameRefMonitor::class.java
                    )
                helperConstructor.isAccessible = true
                helperConstructor.newInstance(sharedContext,
                    handler,
                    alignTimestamps,
                    yuvConverter,
                    frameRefMonitor)
            }.onFailure {
                Logging.e("SurfaceTextureHelper", "$threadName create failure", it)
                return@Callable null
            }.onSuccess {
                return@Callable it
            }
            return@Callable null
        })
    }
    @JvmStatic
    fun create(context: Context, threadName: String, sharedContext: EglBase.Context?): SurfaceTextureHelper? {
        return create(context, threadName, sharedContext, false, YuvConverter(), null as FrameRefMonitor?)
    }
}
