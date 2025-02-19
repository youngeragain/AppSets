package xcj.app.appsets.server.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import xcj.app.starter.foundation.http.DesignResponse

interface QRCodeApi {
    @POST("/login/qrcode_code/gen")
    suspend fun genQRCodeCode(
        @QueryMap(encoded = false) query: Map<String, String?>,
        @Body providerInfo: Map<String, String?>
    ): DesignResponse<Map<String, String>>


    @GET("/login/qrcode_code/state")
    suspend fun qrCodeCodeStatus(
        @QueryMap(encoded = false) query: Map<String, String?>
    ): DesignResponse<Map<String, String?>>


    @POST("/login/qrcode_code/scan")
    suspend fun scanQRCode(
        @QueryMap(encoded = false) query: Map<String, String?>,
        @Body scannerInfo: Map<String, String?>
    ): DesignResponse<Map<String, String>>


    @POST("/login/qrcode_code/confirm")
    suspend fun confirmQRCode(
        @QueryMap(encoded = false) query: Map<String, String?>,
        @Body confirmExtra: Map<String, String?>
    ): DesignResponse<Map<String, String>>
}


