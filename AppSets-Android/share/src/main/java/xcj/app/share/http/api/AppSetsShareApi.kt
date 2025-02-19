package xcj.app.share.http.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import xcj.app.starter.foundation.http.DesignResponse

/**
 * client use it
 */
interface AppSetsShareApi {
    @GET("/appsets/share")
    suspend fun greeting(): DesignResponse<String>

    @GET("/appsets/share/ping")
    suspend fun ping(): DesignResponse<String>

    @GET("/appsets/share/pin/isneed")
    suspend fun isNeedPin(): DesignResponse<Boolean>

    @Headers("Content-Type:text/plain")
    @POST("/appsets/share/pair")
    suspend fun pair(
        @Body pin: Int
    ): DesignResponse<Boolean>

    @Headers("Content-Type:text/plain")
    @POST("/appsets/share/pair_response")
    suspend fun pairResponse(
        @Body token: String
    ): DesignResponse<Boolean>

    @Headers("Content-Type:text/plain")
    @POST("/appsets/share/text")
    suspend fun postText(
        @Header("share_token") token: String,
        @Body text: String
    ): DesignResponse<Boolean>

    @Multipart
    @POST("/appsets/share/file")
    suspend fun postFile(
        @Header("share_token") token: String,
        @Part multiPartBodyPart: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): DesignResponse<Boolean>

    @Headers("Content-Type:text/plain")
    @POST("/appsets/share/prepare")
    suspend fun prepareSend(
        @Header("share_token") token: String
    ): DesignResponse<Boolean>

    @Headers("Content-Type:text/plain")
    @POST("/appsets/share/prepare_response")
    suspend fun prepareSendResponse(
        @Header("share_token") token: String,
        @Body isAccept: Boolean
    ): DesignResponse<Boolean>
}