package xcj.app.appsets.ui.compose.camera

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.usecase.QrCodeUseCase

class CameraComposeViewModel : ViewModel() {

    private val qrCodeUseCase: QrCodeUseCase = QrCodeUseCase(viewModelScope, null)

    val usedFor: MutableState<String?> = mutableStateOf(null)

    //first is providerId, second is code
    val codeState: MutableState<Pair<String, String>?> = mutableStateOf(null)

    fun updateCode(barcode: Barcode) {
        //asqr:login:[providerId]:[code]
        if (barcode.rawValue?.startsWith("asqr") == false) {
            Log.e("CameraComposeViewModel", "code found but not support")
            return
        }
        val split = barcode.rawValue?.split(":") ?: return
        if (split.size > 2) {
            usedFor.value = split[1]
            if (split[1] == "login") {
                if (split.size == 4) {
                    codeState.value = split[2] to split[3]
                }
            }
        }
    }

    fun doScanAction(context: Context) {
        (context as CameraComposeActivity).setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("providerId", codeState.value?.first)
            putExtra("code", codeState.value?.second)
        })
        if (!LocalAccountManager.isLogged())
            context.finish()
        if (codeState.value == null)
            return
        if (usedFor.value == "login") {
            qrCodeUseCase.doScan(codeState.value!!.first, codeState.value!!.second)
        }
    }

    fun doConfirmAction() {
        if (codeState.value == null)
            return
        if (usedFor.value == "login") {
            qrCodeUseCase.doConfirm(codeState.value!!.first, codeState.value!!.second)
        }
    }


}