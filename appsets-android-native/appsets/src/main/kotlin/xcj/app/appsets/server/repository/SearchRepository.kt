package xcj.app.appsets.server.repository

import xcj.app.appsets.server.api.SearchApi
import xcj.app.appsets.server.model.CombineSearchRes
import xcj.app.core.foundation.http.DesignResponse


class SearchRepository(private val searchApi: SearchApi) {
    suspend fun commonSearch(keywords: String): DesignResponse<CombineSearchRes> {
        return searchApi.commonSearch(keywords)
    }
}