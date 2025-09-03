package xcj.app.appsets.ui.compose.camera

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.barcode.common.Barcode
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.page_state.LoginSignUpPageState
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel
import xcj.app.starter.android.util.PurpleLogger

class DesignCameraViewModel : AnyStateViewModel() {

    companion object {
        private const val TAG = "CameraComposeViewModel"
    }

    init {
        PurpleLogger.current.d(TAG, "init")
    }

    val qrCodeUseCase: QRCodeUseCase = QRCodeUseCase(
        mutableStateOf(LoginSignUpPageState.Nothing),
        QRCodeRepository.getInstance(),
        UserRepository.getInstance()
    )

    fun updateCode(activity: Activity, barcode: Barcode) {
        qrCodeUseCase.onScannedBarcode(activity, barcode)
    }
}