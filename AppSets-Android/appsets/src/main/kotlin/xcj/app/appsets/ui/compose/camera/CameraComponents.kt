package xcj.app.appsets.ui.compose.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import xcj.app.appsets.annotation.PleaseAvoidQuickMultiClick
import xcj.app.appsets.util.ktx.writeBitmap
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraComponents {
    companion object {
        private const val TAG = "CameraComponents"
    }

    private lateinit var mainExecutor: Executor
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraTakeExecutor: ExecutorService
    private lateinit var cameraController: LifecycleCameraController
    private var preview: PreviewView? = null

    fun prepare(context: Context) {
        mainExecutor = ContextCompat.getMainExecutor(context)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraTakeExecutor = Executors.newSingleThreadExecutor()
        cameraController = LifecycleCameraController(context)
    }

    fun attachPreview(preview: PreviewView){
        this.preview = preview
    }

    fun detachPreview(){
        this.preview = null
    }

    fun getCameraExecutor(): Executor{
        return cameraExecutor
    }

    fun getCameraController(): CameraController{
        return cameraController
    }

    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
    ) {
        PurpleLogger.current.d(TAG, "bindToLifecycle")
        cameraController.unbind()
        cameraController.bindToLifecycle(lifecycleOwner)
        //preview.setOnTouchListener { _, _ -> false } //no-op
    }

    fun close() {
        PurpleLogger.current.d(TAG, "close")
        detachPreview()
        removeImageAnalysisAnalyzer()
        stopCamera()
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
    fun takePicture(onPictureTaken: (File) -> Unit) {
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
                    onPictureTaken(file)
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

    fun clearOverlayIfNeeded() {
        preview?.overlay?.clear()
    }

    fun setImageAnalysisAnalyzer(mlKitAnalyzer: MlKitAnalyzer) {
        cameraController.setImageAnalysisAnalyzer(cameraExecutor, mlKitAnalyzer)
    }

    fun removeImageAnalysisAnalyzer() {
        cameraController.clearImageAnalysisAnalyzer()
    }
}