package xcj.app.appsets.usecase

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.compose.camera.DesignCameraActivity
import xcj.app.appsets.ui.model.LoginSignUpState
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestNotNullRaw
import xcj.app.starter.util.QrCodeUtil

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

class QRCodeUseCase(
    private val coroutineScope: CoroutineScope,
    private val loginSignUpState: MutableState<LoginSignUpState>,
    private val qrCodeRepository: QRCodeRepository,
    private val userRepository: UserRepository
) : IComposeLifecycleAware {
    companion object {
        private const val TAG = "QRCodeUseCase"
        const val APPSETS_DESIGN_URI_QRCODE_PREFIX = "asqr"
        const val APPSETS_DESIGN_URI_SPLIT_CHAR = ":"
        const val QR_USED_FOR_LOGIN = "login"
        const val QR_USED_FOR_APPSETS_SHARE = "appsets_share"
        const val PROVIDER_ID = "providerId"
        const val CODE = "code"
        const val STATE = "state"
        const val EXTRA = "extra"

        const val QR_STATE_NO_EXIST_OR_EXPIRED = "-1"
        const val QR_STATE_NEW = "0"
        const val QR_STATE_SCANNED = "1"
        const val QR_STATE_CONFIRMED = "2"
    }

    private var updateQRCodeStateJob: Job? = null

    val generatedQRCodeInfo: MutableState<QRCodeInfoScannedState.AppSetsQRCodeInfo?> =
        mutableStateOf(null)

    //first is providerId, second is code
    val scannedQRCodeInfo: MutableState<QRCodeInfoScannedState?> = mutableStateOf(null)

    fun requestGenerateQRCode() {
        updateQRCodeStateJob?.cancel("request generate new QRCode!")
        generatedQRCodeInfo.value = null
        coroutineScope.launch {
            requestNotNull(
                action = {
                    qrCodeRepository.genQRCodeCode(null)
                },
                onSuccess = {
                    handleGeneratedQRCode(it)
                }
            )
        }
    }

    suspend fun handleGeneratedQRCode(qrCodeInfoMap: Map<String, String>) {
        val providerId = qrCodeInfoMap[PROVIDER_ID] ?: return
        val code = qrCodeInfoMap[CODE] ?: return
        val state = qrCodeInfoMap[STATE] ?: QR_STATE_NO_EXIST_OR_EXPIRED
        //asqr:login:provider_id:code:state
        val qrcodeRawContent = String.format(
            "%s%s%s%s%s%s%s%s%s",
            APPSETS_DESIGN_URI_QRCODE_PREFIX,
            APPSETS_DESIGN_URI_SPLIT_CHAR,
            QR_USED_FOR_LOGIN,
            APPSETS_DESIGN_URI_SPLIT_CHAR,
            providerId,
            APPSETS_DESIGN_URI_SPLIT_CHAR,
            code,
            APPSETS_DESIGN_URI_SPLIT_CHAR,
            state
        )
        val qrcodeBitmap = QrCodeUtil.encodeAsBitmap(qrcodeRawContent) ?: return

        withContext(Dispatchers.Main) {
            generatedQRCodeInfo.value = QRCodeInfoScannedState.AppSetsQRCodeInfo(
                state = state,
                providerId = providerId,
                code = code,
                bitmap = qrcodeBitmap
            )
        }
        continueQueryQRCodeStatus(providerId, code)
    }

    fun continueQueryQRCodeStatus(providerId: String, code: String) {
        if (providerId.isEmpty()) {
            return
        }
        updateQRCodeStateJob?.cancel()
        coroutineScope.launch {
            for (i in 0..13) {
                coroutineScope.launch {
                    requestNotNullRaw(
                        action = {
                            qrCodeRepository.qrCodeCodeStatus(providerId, code)
                        },
                        onSuccess = {
                            updateQRCodeStatus(it)
                        }
                    )
                }
                delay(5000)
            }
        }
    }

    private suspend fun updateQRCodeStatus(qrCodeStatusMapResponse: DesignResponse<Map<String, String?>>) {
        val qrCodeStatusMap = qrCodeStatusMapResponse.data
        if (qrCodeStatusMap != null) {
            val qrCodeState = qrCodeStatusMap[STATE] ?: QR_STATE_NO_EXIST_OR_EXPIRED
            val exitQRCodeInfo = generatedQRCodeInfo.value
            if (exitQRCodeInfo != null) {
                withContext(Dispatchers.Main) {
                    generatedQRCodeInfo.value = exitQRCodeInfo.copy(state = qrCodeState)
                }
            }
            if (qrCodeState == QR_STATE_SCANNED) {
                if (LocalAccountManager.isLogged()) {

                }
            }
            if (qrCodeState == QR_STATE_CONFIRMED) {
                if (LocalAccountManager.isLogged()) {
                    //已经登录了，则此处无需操作
                } else {
                    //已授权给予登录，拿到token，该token为对方的token
                    val tempToken = qrCodeStatusMap[EXTRA]?.split("=")?.get(1) ?: ""
                    startQuickLogin(tempToken)
                    updateQRCodeStateJob?.cancel("qrcode operated!")
                }
            }
        } else {
            if (qrCodeStatusMapResponse.info == "not exist or expired!") {
                val exitQRCodeInfo = generatedQRCodeInfo.value
                if (exitQRCodeInfo != null) {
                    withContext(Dispatchers.Main) {
                        generatedQRCodeInfo.value =
                            exitQRCodeInfo.copy(state = QR_STATE_NO_EXIST_OR_EXPIRED)
                    }
                }
            }
        }
    }


    private fun startQuickLogin(
        token: String
    ) {
        val oldLoginSignUpState = loginSignUpState.value
        if (oldLoginSignUpState is LoginSignUpState.Logging) {
            return
        }
        loginSignUpState.value = LoginSignUpState.Logging
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    LocalAccountManager.onUserLogged(UserInfo.default(), token, true)
                    val loginResponse = userRepository.login2()
                    val token = loginResponse.data
                    if (!loginResponse.success || token.isNullOrEmpty()) {
                        PurpleLogger.current.d(TAG, loginResponse.info)
                        loginSignUpState.value = LoginSignUpState.LoggingFail
                        return@requestNotNullRaw
                    }
                    LocalAccountManager.onUserLogged(UserInfo.default(), token, true)
                    val userInfoResponse = userRepository.getLoggedUserInfo()
                    val userInfo = userInfoResponse.data
                    if (userInfo == null) {
                        PurpleLogger.current.d(TAG, "startQuickLogin failed! userInfo is null ")
                        loginSignUpState.value = LoginSignUpState.LoggingFail
                        return@requestNotNullRaw
                    }
                    LocalAccountManager.onUserLogged(userInfo, token, false)
                    loginSignUpState.value = LoginSignUpState.LoggingFinish
                }
            )
        }
    }

    /**
     * 如果是扫码者确认，那么，扫码的一端必须是未登录状态，所需的providerId和code需要从外部提供，即从扫描到的二维码处获取
     * 如果是二维码提供者确认，那么所需的providerId和code可从本地直接获取
     */
    fun doConfirm(outerProviderId: String? = null, outerCode: String? = null) {
        val codeInfoWrapper = generatedQRCodeInfo.value ?: return
        val realProviderId = outerProviderId ?: codeInfoWrapper.providerId ?: return
        val realCode = outerCode ?: codeInfoWrapper.code ?: return
        coroutineScope.launch {
            requestNotNull(
                action = {
                    qrCodeRepository.confirmQRCode(realProviderId, realCode)
                }
            )
        }
    }

    fun onScannedBarcode(activity: Activity, barcode: Barcode) {
        PurpleLogger.current.d(TAG, "onScannedBarcode")
        val barRawString = barcode.rawValue
        //asqr:login:provider_id:code:state
        if (barRawString?.startsWith(APPSETS_DESIGN_URI_QRCODE_PREFIX) == false) {
            PurpleLogger.current.d(TAG, "onScannedBarcode, code found but not support 1")
            makeOthersQRCodeInfo(barRawString)
            return
        }
        val split = barcode.rawValue?.split(APPSETS_DESIGN_URI_SPLIT_CHAR) ?: return
        if (split.size <= 2) {
            PurpleLogger.current.d(TAG, "onScannedBarcode, code found but not support 2")
            makeOthersQRCodeInfo(barRawString)
            return
        }
        val usedFor = split[1]
        when (usedFor) {
            QR_USED_FOR_APPSETS_SHARE -> {
                PurpleLogger.current.d(TAG, "onScannedBarcode, for appsets share")
                val deviceAddresses = split.subList(2, split.size).toTypedArray()
                val intent = Intent()
                intent.putExtra("APPSETS_SHARE_DEVICE_ADDRESSES", deviceAddresses)
                activity.setResult(RESULT_OK, intent)
                activity.finish()
            }

            QR_USED_FOR_LOGIN -> {
                PurpleLogger.current.d(TAG, "onScannedBarcode, for appsets login")
                if (split.size == 5) {
                    val qrCodeProviderId = split[2]
                    val qrCodeCode = split[3]
                    val qrCodeCodeState = split[4]
                    PurpleLogger.current.d(TAG, "onScannedBarcode, update scanned qrcode info!")
                    val scanCount =
                        (scannedQRCodeInfo.value?.scanCount ?: 0) + 1
                    scannedQRCodeInfo.value = QRCodeInfoScannedState.AppSetsQRCodeInfo(
                        providerId = qrCodeProviderId,
                        code = qrCodeCode,
                        state = qrCodeCodeState,
                        scanCount = scanCount,
                    )
                } else {
                    val scanCount =
                        (scannedQRCodeInfo.value?.scanCount ?: 0) + 1
                    scannedQRCodeInfo.value = QRCodeInfoScannedState.AppSetsQRCodeInfo(
                        scanCount = scanCount,
                        isCompatible = false
                    )
                }
            }

            else -> {
                PurpleLogger.current.d(TAG, "onScannedBarcode, for appsets others")
            }
        }
    }

    private fun makeOthersQRCodeInfo(barRawString: String?) {
        PurpleLogger.current.d(TAG, "makeOthersQRCodeInfo")
        val scanCount =
            (scannedQRCodeInfo.value?.scanCount ?: 0) + 1
        scannedQRCodeInfo.value = QRCodeInfoScannedState.OthersQRCodeInfo(
            rawString = barRawString,
            scanCount = scanCount
        )
    }

    fun doScanAction() {
        val qrCodeInfo =
            scannedQRCodeInfo.value as? QRCodeInfoScannedState.AppSetsQRCodeInfo ?: return
        if (qrCodeInfo.usedFor != QR_USED_FOR_LOGIN) {
            return
        }
        coroutineScope.launch {
            requestNotNull(
                action = {
                    qrCodeRepository.scanQRCode(qrCodeInfo.providerId ?: "", qrCodeInfo.code ?: "")
                }
            )
        }
    }

    fun doAfterScanConfirmAction() {
        val qrCodeInfo =
            scannedQRCodeInfo.value as? QRCodeInfoScannedState.AppSetsQRCodeInfo ?: return
        if (qrCodeInfo.usedFor != QR_USED_FOR_LOGIN) {
            return
        }
        doConfirm(qrCodeInfo.providerId ?: "", qrCodeInfo.code ?: "")
    }

    override fun onComposeDispose(by: String?) {
        updateQRCodeStateJob?.cancel()
        generatedQRCodeInfo.value = null
    }

    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, intent: Intent) {
        if (requestCode != DesignCameraActivity.REQUEST_CODE ||
            resultCode != RESULT_OK
        ) {
            return
        }
        val providerId = intent.getStringExtra(PROVIDER_ID) ?: return
        val code = intent.getStringExtra(CODE) ?: return
        continueQueryQRCodeStatus(providerId, code)
    }
}