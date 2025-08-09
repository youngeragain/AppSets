package xcj.app.appsets.ui.compose.camera

import android.app.Activity
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.model.LoginSignUpState
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
        viewModelScope,
        mutableStateOf(LoginSignUpState.Nothing),
        QRCodeRepository.getInstance(),
        UserRepository.getInstance()
    )

    fun updateCode(activity: Activity, barcode: Barcode) {
        qrCodeUseCase.onScannedBarcode(activity, barcode)
    }
}