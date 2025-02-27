package xcj.app.appsets.ui.compose.camera

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PurpleLogger
import kotlin.collections.first

class CameraComposeActivity : DesignComponentActivity() {
    companion object {
        const val REQUEST_CODE = 9999
        private const val TAG = "CameraComposeActivity"
    }

    private lateinit var barcodeScanner: BarcodeScanner
    private val cameraComponents: CameraComponents = CameraComponents()
    private val viewModel by viewModels<CameraComposeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraComponents.create(this)
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

    fun startCamera(previewView: PreviewView) {
        cameraComponents.bindToLifecycle(this, previewView) { executor ->
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            barcodeScanner = BarcodeScanning.getClient(options)

            setImageAnalysisAnalyzer(
                executor,
                MlKitAnalyzer(
                    listOf(barcodeScanner),
                    COORDINATE_SYSTEM_VIEW_REFERENCED,
                    ContextCompat.getMainExecutor(this@CameraComposeActivity)
                ) { result: MlKitAnalyzer.Result ->
                    val barcodeResults = result.getValue(barcodeScanner)
                    if ((barcodeResults == null) ||
                        (barcodeResults.isEmpty()) ||
                        (barcodeResults.first() == null)
                    ) {
                        previewView.overlay.clear()
                        return@MlKitAnalyzer
                    }
                    viewModel.updateCode(this@CameraComposeActivity, barcodeResults[0])
                }
            )
        }
        cameraComponents.startCamera()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraComponents.close()
        barcodeScanner.close()
    }
}