package xcj.app.share.http.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming
import xcj.app.share.base.ShareDevice
import xcj.app.share.http.base.HttpShareDevice
import xcj.app.share.http.model.ContentInfoListWrapper
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

    @POST("/appsets/share/pair")
    suspend fun pair(
        @Header("pin") pin: Int
    ): DesignResponse<Boolean>

    @POST("/appsets/share/pair_response")
    suspend fun pairResponse(
        @Header("share_token") shareToken: String
    ): DesignResponse<Boolean>

    @Headers("Content-Type:text/plain")
    @POST("/appsets/share/text")
    suspend fun postText(
        @Header("share_token") shareToken: String,
        @Body text: String
    ): DesignResponse<Boolean>

    @Multipart
    @POST("/appsets/share/file")
    suspend fun postFile(
        @Header("share_token") shareToken: String,
        @Part multiPartBodyPart: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): DesignResponse<Boolean>

    @POST("/appsets/share/prepare")
    suspend fun prepareSend(
        @Header("share_token") shareToken: String,
        @Header("uri") uri: String,
    ): DesignResponse<Boolean>

    @POST("/appsets/share/prepare_response")
    suspend fun prepareSendResponse(
        @Header("share_token") shareToken: String,
        @Header("is_accept") isAccept: Boolean,
        @Header("prefer_download_self") isPreferDownloadSelf: Boolean,
    ): DesignResponse<Boolean>

    @Streaming
    @GET("/appsets/share/content/get")
    fun getContent(
        @Header("share_token") shareToken: String,
        @Header("content_id") contentId: String,
    ): retrofit2.Call<ResponseBody>

    @POST("/appsets/share/contents/get")
    suspend fun getContentList(
        @Header("share_token") shareToken: String,
        @Header("uri") uri: String
    ): DesignResponse<ContentInfoListWrapper>

    @Headers("Content-Type:application/json")
    @POST("/appsets/share/device/info/exchange")
    suspend fun exchangeDeviceInfo(
        @Body shareDevice: HttpShareDevice,
    ): DesignResponse<HttpShareDevice>
}