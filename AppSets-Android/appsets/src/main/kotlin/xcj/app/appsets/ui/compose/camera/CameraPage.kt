package xcj.app.appsets.ui.compose.camera

import android.app.Activity
import androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.custom_component.DesignBottomBackButton
import xcj.app.appsets.usecase.QRCodeInfoScannedState
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "CameraPage"

@Composable
fun CameraPage(
    onBackClick: () -> Unit
) {
    val viewModel = viewModel<DesignCameraViewModel>()
    CompositionLocalProvider(
        LocalUseCaseOfQRCode provides viewModel.qrCodeUseCase,
        LocalAnyStateProvider provides viewModel
    ) {
        CameraContent(modifier = Modifier, onBackClick = onBackClick)
        BottomSheetContainer()
    }
}

@Composable
fun CameraContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val qrCodeUseCase = LocalUseCaseOfQRCode.current
    val anyStateProvider = LocalAnyStateProvider.current

    val viewModel = viewModel<DesignCameraViewModel>()
    val cameraComponents = remember {
        CameraComponents().apply {
            prepare(context)
        }
    }

    val composeContainerState =
        anyStateProvider.bottomSheetState()

    val scannedQRCodeInfo by qrCodeUseCase.scannedQRCodeInfo

    DisposableEffect(Unit) {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val barcodeScanner = BarcodeScanning.getClient(options)
        val mlKitAnalyzer = MlKitAnalyzer(
            listOf(barcodeScanner),
            COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(context)
        ) { result: MlKitAnalyzer.Result ->
            val barcodeResults = result.getValue(barcodeScanner)
            if ((barcodeResults == null) ||
                (barcodeResults.isEmpty()) ||
                (barcodeResults.first() == null)
            ) {
                cameraComponents.clearOverlayIfNeeded()
                return@MlKitAnalyzer
            }
            if(context is Activity){
                viewModel.updateCode(context, barcodeResults[0])
            }
        }
        cameraComponents.setImageAnalysisAnalyzer(mlKitAnalyzer)
        onDispose {
            cameraComponents.close()
        }
    }

    LaunchedEffect(scannedQRCodeInfo) {
        if (scannedQRCodeInfo == null) {
            PurpleLogger.current.d(TAG, "LaunchedEffect, composeContainerState.hide()")
            composeContainerState.hide()
        } else {
            PurpleLogger.current.d(TAG, "LaunchedEffect, composeContainerState.show()")
            composeContainerState.show {
                val qRCodeInfoScannedState = scannedQRCodeInfo
                if(qRCodeInfoScannedState ==null){
                    return@show
                }
                when (qRCodeInfoScannedState) {
                    is QRCodeInfoScannedState.AppSetsQRCodeInfo -> {
                        AppSetsQRCodeInfoHandlerSheetContent(qRCodeInfoScannedState)
                    }

                    is QRCodeInfoScannedState.OthersQRCodeInfo -> {
                        OthersQRCodeInfoHandlerSheetContent(qRCodeInfoScannedState)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                val preview = PreviewView(it)
                preview
            }
        ) {
            cameraComponents.bindToLifecycle(lifecycleOwner)
            cameraComponents.attachPreview(it)
            cameraComponents.startCamera()
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .systemBarsPadding(),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Icon(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_outline_qr_code_scanner_24),
                contentDescription = "scanning",
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .padding(12.dp)
            )
        }
        DesignBottomBackButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = onBackClick
        )
    }
}

@Composable
fun OthersQRCodeInfoHandlerSheetContent(scannedQRCodeInfo: QRCodeInfoScannedState.OthersQRCodeInfo) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(xcj.app.appsets.R.string.founded_unsupport_qr_code),
            fontSize = 16.sp
        )
        SelectionContainer {
            Text(
                scannedQRCodeInfo.rawString ?: "",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AppSetsQRCodeInfoHandlerSheetContent(
    appSetsQRCodeInfo: QRCodeInfoScannedState.AppSetsQRCodeInfo,
) {
    val qrCodeUseCase = LocalUseCaseOfQRCode.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(xcj.app.appsets.R.string.founded_support_qr_code),
            fontSize = 16.sp
        )
        if (appSetsQRCodeInfo.isCompatible) {
            Row(modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when (appSetsQRCodeInfo.state) {
                    QRCodeUseCase.QR_STATE_NEW -> {
                        FilledTonalButton(
                            onClick = {
                                qrCodeUseCase.doScanAction()
                            }
                        ) {
                            Text(text = stringResource(xcj.app.appsets.R.string.scan))
                        }
                    }

                    QRCodeUseCase.QR_STATE_SCANNED -> FilledTonalButton(
                        onClick = {
                            qrCodeUseCase.doAfterScanConfirmAction()
                        }
                    ) {
                        Text(text = stringResource(id = xcj.app.starter.R.string.ok))
                    }

                    QRCodeUseCase.QR_STATE_CONFIRMED -> Text(text = stringResource(id = xcj.app.appsets.R.string.done))
                    QRCodeUseCase.QR_STATE_NO_EXIST_OR_EXPIRED -> Text(text = stringResource(id = xcj.app.appsets.R.string.invalid))
                    else -> Text(text = stringResource(id = xcj.app.appsets.R.string.unknown_state))
                }
            }
        } else {
            Text(text = stringResource(id = xcj.app.appsets.R.string.not_compatible))
        }
    }
}