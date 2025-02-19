package xcj.app.appsets.server.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.api.QRCodeApi
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.util.DeviceInfoHelper
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse

class QRCodeRepository(private val qrCodeApi: QRCodeApi) {

    private val gson = Gson()


    suspend fun genQRCodeCode(providerId: String? = null): DesignResponse<Map<String, String>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "genQRCodeCode, thread:${Thread.currentThread()}")
            val deviceInfoJson = gson.toJson(DeviceInfoHelper.provideInfo())
            return@withContext qrCodeApi.genQRCodeCode(
            hashMapOf(
                "force" to "false",
            ).apply {
                providerId?.let {
                    put("providerId", it)
                }
            },
                mapOf("content" to deviceInfoJson)
        )
    }

    suspend fun qrCodeCodeStatus(
        providerId: String,
        code: String
    ): DesignResponse<Map<String, String?>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "qrCodeCodeStatus, thread:${Thread.currentThread()}")
        return@withContext qrCodeApi.qrCodeCodeStatus(
            mapOf(
                "providerId" to providerId,
                "code" to code
            )
        )
    }

    suspend fun scanQRCode(providerId: String, code: String): DesignResponse<Map<String, String>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "scanQRCode, thread:${Thread.currentThread()}")
            val deviceInfoJson = gson.toJson(DeviceInfoHelper.provideInfo())
            return@withContext qrCodeApi.scanQRCode(
            mapOf(
                "providerId" to providerId,
                "code" to code
            ),
                mapOf("text" to deviceInfoJson)
        )
    }

    suspend fun confirmQRCode(
        providerId: String,
        code: String
    ): DesignResponse<Map<String, String>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "confirmQRCode, thread:${Thread.currentThread()}")
        return@withContext qrCodeApi.confirmQRCode(
            mapOf(
                "providerId" to providerId,
                "code" to code
            ),
            hashMapOf(
                "text" to "token=${LocalAccountManager.token}"
            )
        )
    }

    companion object {

        private const val TAG = "QRCodeRepository"
        private var INSTANCE: QRCodeRepository? = null

        fun getInstance(): QRCodeRepository {
            if (INSTANCE == null) {
                val api = ApiProvider.provide(QRCodeApi::class.java)
                val repository = QRCodeRepository(api)
                INSTANCE = repository
            }
            return INSTANCE!!
        }
    }
}