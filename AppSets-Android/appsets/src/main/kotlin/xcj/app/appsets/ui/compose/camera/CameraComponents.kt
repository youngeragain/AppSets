package xcj.app.appsets.ui.compose.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import xcj.app.appsets.annotation.PleaseAvoidQuickMultiClick
import xcj.app.appsets.util.ktx.writeBitmap
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraComponents {
    companion object {
        private const val TAG = "CameraComponents"
    }

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraTakeExecutor: ExecutorService
    private lateinit var cameraController: LifecycleCameraController
    private var preview: PreviewView? = null

    fun create(context: Context) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraTakeExecutor = Executors.newSingleThreadExecutor()
        cameraController = LifecycleCameraController(context)
    }

    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        preview: PreviewView,
        controllerApplier: (LifecycleCameraController.(ExecutorService) -> Unit)? = null,
    ) {
        PurpleLogger.current.d(TAG, "bindToLifecycle")
        cameraController.unbind()
        controllerApplier?.invoke(cameraController, cameraExecutor)
        cameraController.bindToLifecycle(lifecycleOwner)
        //preview.setOnTouchListener { _, _ -> false } //no-op
        this.preview = preview
    }

    fun close() {
        PurpleLogger.current.d(TAG, "close")
        cameraController.unbind()
        cameraExecutor.shutdown()
        cameraTakeExecutor.shutdown()
    }

    fun startCamera() {
        PurpleLogger.current.d(TAG, "startCamera")
        preview?.controller = cameraController
    }

    fun stopCamera() {
        PurpleLogger.current.d(TAG, "stopCamera")
        cameraController.unbind()
    }

    @PleaseAvoidQuickMultiClick
    fun takePicture(pictureFileCallback: (File) -> Unit) {
        PurpleLogger.current.d(TAG, "takePicture")
        cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        cameraController.takePicture(
            cameraTakeExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureStarted() {
                    PurpleLogger.current.d(TAG, "takePicture, onCaptureStarted")
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    PurpleLogger.current.d(TAG, "takePicture, onCaptureSuccess")
                    val image = image.toBitmap()
                    val fileName = "${UUID.randomUUID()}.png"
                    val file = File(
                        LocalAndroidContextFileDir.current.tempImagesCacheDir,
                        fileName
                    )
                    file.writeBitmap(image, Bitmap.CompressFormat.PNG, 65)
                    PurpleLogger.current.d(
                        TAG,
                        "takePicture, onCaptureSuccess, finish!, picture:$file"
                    )
                    pictureFileCallback(file)
                }

                override fun onCaptureProcessProgressed(progress: Int) {
                    PurpleLogger.current.d(
                        TAG,
                        "takePicture, onCaptureProcessProgressed, progress:${progress}"
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    PurpleLogger.current.d(TAG, "takePicture, onError, e:${exception.message}")
                }

                override fun onPostviewBitmapAvailable(bitmap: Bitmap) {
                    PurpleLogger.current.d(TAG, "takePicture, onPostviewBitmapAvailable")
                }
            })
    }
}