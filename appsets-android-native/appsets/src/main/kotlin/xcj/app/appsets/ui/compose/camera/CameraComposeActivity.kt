package xcj.app.appsets.ui.compose.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraComposeActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    val vm by viewModels<CameraComposeViewModel>()
    /*lateinit var cameraManager:CameraManager
    fun init1(){
        cameraManager.requestPreviewFrame(object :Handler(Looper.myLooper()!!){
            override fun dispatchMessage(msg: Message) {

            }
        }, 898)
    }*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //cameraManager = CameraManager(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraContent("Android")
                }
            }
        }
    }

    fun startCamera(previewView: PreviewView) {

        val cameraController = LifecycleCameraController(baseContext)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            cameraExecutor,
            MlKitAnalyzer(
                listOf(barcodeScanner),
                CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if ((barcodeResults == null) ||
                    (barcodeResults.size == 0) ||
                    (barcodeResults.first() == null)
                ) {
                    previewView.overlay.clear()
                    previewView.setOnTouchListener { _, _ -> false } //no-op
                    return@MlKitAnalyzer
                }
                vm.updateCode(barcodeResults[0])


/*
                val qrCodeViewModel = QrCodeViewModel(barcodeResults[0])
                val qrCodeDrawable = QrCodeDrawable(qrCodeViewModel)

                previewView.setOnTouchListener(qrCodeViewModel.qrCodeTouchCallback)
                previewView.overlay.clear()
                previewView.overlay.add(qrCodeDrawable)*/
            }
        )

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }
    override fun onResume() {
        super.onResume()
        //init1()
        //cameraManager.startPreview()
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        barcodeScanner.close()
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CameraContent(name: String, modifier: Modifier = Modifier) {
    /*AndroidView(factory = {
        val surfaceView = SurfaceView(it)
        val cameraComposeActivity = it as CameraComposeActivity
        val cameraManager = cameraComposeActivity.cameraManager
        cameraManager.openDriver(surfaceView.holder)
        *//*cameraManager.startPreview()*//*
        surfaceView
    }, modifier = modifier){

    }*/
    Box {
        AndroidView(factory = {
            val preview = PreviewView(it)
            preview
        }, modifier = modifier.fillMaxSize()) {
            (it.context as CameraComposeActivity).startCamera(it)
        }
        val vm = viewModel<CameraComposeViewModel>(LocalContext.current as ComponentActivity)
        AnimatedVisibility(vm.usedFor.value != null, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    "发现受支持的二维码",
                    fontSize = 32.sp,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                    val context = LocalContext.current
                    Button(onClick = {
                        vm.doScanAction(context)
                    }) {
                        Text(text = "扫描")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        vm.doConfirmAction()
                    }) {
                        Text(text = "确定")
                    }
                }
            }
        }
    }
}