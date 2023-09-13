package xcj.app.appsets.server.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.server.model.SpotLight
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.usecase.models.Application
import xcj.app.core.foundation.http.DesignResponse

interface AppSetsApi:URLApi {

    @POST("appsets/apptoken/get")
    suspend fun getAppToken(@Body hashMap: Map<String, String?>): DesignResponse<String?>

    @POST("appsets/apps/index/recommend")
    suspend fun getIndexApplications(): DesignResponse<List<AppsWithCategory>?>

    @GET("appsets/application/user/{uid}")
    suspend fun getUsersApplications(@Path("uid") uid: String): DesignResponse<List<Application>?>

    @GET("appsets/spotlight")
    suspend fun getSpotLight(): DesignResponse<SpotLight?>

    @GET("appsets/client/update")
    suspend fun checkUpdate(
        @Query("versionCode") versionCode: Int,
        @Header("platform") platform: String
    ): DesignResponse<UpdateCheckResult?>

    @GET("appsets/client/update/history")
    suspend fun getUpdateHistory(
        @Query("min") minVersionCode: String?,
        @Header("platform") platform: String
    ): DesignResponse<List<UpdateCheckResult>?>

    @GET("appsets/app/create/precheck")
    suspend fun createApplicationPreCheck(@Query("appName") appName: String): DesignResponse<Boolean>

    @POST("appsets/app/create")
    suspend fun createApplication(@Body body: HashMap<String, Any?>): DesignResponse<Boolean>

}
