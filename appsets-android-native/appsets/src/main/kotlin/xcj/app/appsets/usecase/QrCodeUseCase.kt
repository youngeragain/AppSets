package xcj.app.appsets.usecase

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.api.QRCodeApi
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.UserLoginRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.compose.camera.CameraComposeActivity
import xcj.app.core.android.ktx.dp
import java.util.EnumMap

class QrCodeUseCase(
    private val coroutineScope: CoroutineScope,
    private var loginState: MutableState<UserLoginUseCase.LoginSignUpState?>?
) {
    private val userLoginRepository: UserLoginRepository = UserLoginRepository(
        URLApi.provide(
            UserApi::class.java
        )
    )
    private val userRepository: UserRepository = UserRepository(URLApi.provide(UserApi::class.java))
    private val qrCodeRepository = QRCodeRepository(URLApi.provide(QRCodeApi::class.java))
    val qrcodeState: MutableState<Pair<Bitmap?, String>?> = mutableStateOf(null)

    //扫描二维码后等待对方操作
    val waitPeerState: MutableState<Boolean?> = mutableStateOf(null)
    var getStateJob: Job? = null
    var providerId: String? = null

    fun genQrCode(usedFor: String = "login") {
        getStateJob?.cancel("new qrcode generated!")
        coroutineScope.launch(Dispatchers.IO) {
            providerId = null
            val genQRCodeCode = qrCodeRepository.genQRCodeCode(providerId)
            if (genQRCodeCode.data != null) {
                providerId = genQRCodeCode.data!!.get("providerId") ?: return@launch
                val code = genQRCodeCode.data!!.get("code") ?: return@launch
                val encodeAsBitmap = encodeAsBitmap("asqr:login:$providerId:$code") ?: return@launch
                val state = genQRCodeCode.data!!.get("state") ?: "-1"

                withContext(Dispatchers.Main) {
                    qrcodeState.value = encodeAsBitmap to state
                }
                getQrCodeStatus(providerId!!, code)
            }
        }
    }

    fun getQrCodeStatus(providerId: String, code: String) {
        getStateJob?.cancel()
        getStateJob = coroutineScope.launch a@{
            if (providerId.isEmpty())
                return@a
            for (i in 0..65) {
                val stateRes = qrCodeRepository.qrCodeCodeStatus(providerId, code)
                if (stateRes.data != null) {
                    val state1 = stateRes.data!!.get("state") ?: "-1"
                    if (qrcodeState.value != null) {
                        withContext(Dispatchers.Main) {
                            qrcodeState.value = qrcodeState.value!!.first to state1
                        }
                    }
                    if(state1=="1"){
                        if(LocalAccountManager.isLogged()){

                        }
                    }
                    if(state1=="2"){
                        if(!LocalAccountManager.isLogged()){
                            //已授权给予登录，拿到token，该token为对方的token
                            startLogin2(
                                stateRes.data!!.get("extra")?.split("=")?.get(1) ?: "",
                                loginState
                            )
                            getStateJob?.cancel("qrcode operated!")
                        }else{
                            //已经登录了，则此处无需操作
                        }
                        return@a
                    }
                }else {
                    if(stateRes.info=="not exist or expired!"){
                        if(qrcodeState.value!=null){
                            withContext(Dispatchers.Main){
                                qrcodeState.value = qrcodeState.value!!.first to "-1"
                            }
                        }
                        return@a
                    }
                }
                delay(1000)
            }
        }
    }


    private fun startLogin2(
        token: String,
        loginState: MutableState<UserLoginUseCase.LoginSignUpState?>?
    ) {
        if (loginState?.value is UserLoginUseCase.LoginSignUpState.Logining)
            return
        loginState?.value = UserLoginUseCase.LoginSignUpState.Logining()
        coroutineScope.launch(Dispatchers.IO) {
            LocalAccountManager.initNonPersistenceToken(token)
            val loginRes = userLoginRepository.login2()
            if (!loginRes.success || loginRes.data.isNullOrEmpty()) {
                Log.e("QrCodeUseCase", loginRes.info ?: "")
                loginState?.value = UserLoginUseCase.LoginSignUpState.LoggingFail()
                return@launch
            }
            LocalAccountManager.initNonPersistenceToken(loginRes.data)
            val userInfoRes = userRepository.getLoggedUserInfo()
            if (!userInfoRes.success || userInfoRes.data == null) {
                Log.e("QrCodeUseCase", "登录2失败")
                loginState?.value = UserLoginUseCase.LoginSignUpState.LoggingFail()
                return@launch
            }
            LocalAccountManager.saveUserInfo(userInfoRes.data!!)
            LocalAccountManager.initPersistenceToken(token)
            delay(2000)
            loginState?.value = UserLoginUseCase.LoginSignUpState.LoggingFinish
        }
    }


    @Throws(WriterException::class)
    fun encodeAsBitmap(contents:String?): Bitmap? {
        val contentsToEncode: String = contents ?: return null
        val hints: MutableMap<EncodeHintType?, Any?> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8.name()
        val result: BitMatrix = try {
            val dimension = 240.dp()
            MultiFormatWriter().encode(contentsToEncode, BarcodeFormat.QR_CODE, dimension, dimension, hints)
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }
        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result[x, y]) BLACK else WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun toScanQrCodePage(context: Context) {
        (context as? AppCompatActivity)?.startActivityForResult(Intent(context, CameraComposeActivity::class.java), 9999)
    }

    fun doScan(providerId:String, code:String){
        coroutineScope.launch(Dispatchers.IO) {
            qrCodeRepository.scanQRCode(providerId, code)
        }
    }

    /**
     * 如果是扫码者确认，那么，扫码的一端必须是未登录状态，所需的providerId和code需要从外部提供，即从扫描到的二维码处获取
     * 如果是二维码提供者确认，那么所需的providerId和code可从本地直接获取
     */
    fun doConfirm(outerProviderId: String? = null, outerCode: String? = null) {
        coroutineScope.launch(Dispatchers.IO) {
            val realProviderId = outerProviderId ?: providerId ?: return@launch
            val realCode = outerCode ?: qrcodeState.value?.second ?: return@launch
            qrCodeRepository.confirmQRCode(realProviderId, realCode)
        }
    }


    fun onDestroy() {

    }

    private val WHITE = -0x00000001
    private var BLACK = -0x01000000

}