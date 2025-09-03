package xcj.app.appsets.ui.model.state

import android.graphics.Bitmap
import xcj.app.appsets.usecase.QRCodeUseCase

sealed interface QRCodeInfoScannedState {
    val scanCount: Int

    data class AppSetsQRCodeInfo(
        val usedFor: String = QRCodeUseCase.QR_USED_FOR_LOGIN,
        val state: String? = null,
        val providerId: String? = null,
        val code: String? = null,
        val bitmap: Bitmap? = null,
        override val scanCount: Int = 0,
        val isCompatible: Boolean = true
    ) : QRCodeInfoScannedState

    data class OthersQRCodeInfo(
        val rawString: String? = null,
        override val scanCount: Int = 0
    ) : QRCodeInfoScannedState
}