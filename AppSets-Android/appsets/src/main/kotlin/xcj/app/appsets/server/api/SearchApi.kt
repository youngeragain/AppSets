package xcj.app.appsets.server.api

import retrofit2.http.*
import xcj.app.appsets.server.model.CombineSearchRes
import xcj.app.starter.foundation.http.DesignResponse


interface SearchApi {

    @GET("search/common")
    suspend fun commonSearch(
        @Query("keywords") keywords: String,
        @Query("page") page: Int? = 1,
        @Query("size") pageSize: Int? = 20
    ): DesignResponse<CombineSearchRes>
}
