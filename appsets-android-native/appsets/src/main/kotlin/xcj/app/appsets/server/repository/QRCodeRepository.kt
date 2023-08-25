package xcj.app.appsets.server.repository

import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.json
import xcj.app.appsets.server.api.QRCodeApi
import xcj.app.appsets.util.DeviceInfoHelper
import xcj.app.core.foundation.http.DesignResponse

class QRCodeRepository(private val qrCodeApi: QRCodeApi) {
    suspend fun genQRCodeCode(providerId: String? = null): DesignResponse<Map<String, String>> {
        return qrCodeApi.genQRCodeCode(
            hashMapOf(
                "force" to "false",
            ).apply {
                providerId?.let {
                    put("providerId", it)
                }
            },
            mapOf("text" to DeviceInfoHelper.provideInfo().json())
        )
    }

    suspend fun qrCodeCodeStatus(
        providerId: String,
        code: String
    ): DesignResponse<Map<String, String?>> {
        return qrCodeApi.qrCodeCodeStatus(
            mapOf(
                "providerId" to providerId,
                "code" to code
            )
        )
    }

    suspend fun scanQRCode(providerId: String, code: String): DesignResponse<Map<String, String>> {
        return qrCodeApi.scanQRCode(
            mapOf(
                "providerId" to providerId,
                "code" to code
            ),
            mapOf("text" to DeviceInfoHelper.provideInfo().json())
        )
    }

    suspend fun confirmQRCode(
        providerId: String,
        code: String
    ): DesignResponse<Map<String, String>> {
        return qrCodeApi.confirmQRCode(
            mapOf(
                "providerId" to providerId,
                "code" to code
            ),
            hashMapOf(
                "text" to "token=${LocalAccountManager.token}"
            )
        )
    }
}