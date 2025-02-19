package xcj.app.appsets.server.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.server.api.SearchApi
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.server.model.CombineSearchRes
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse

class SearchRepository(private val searchApi: SearchApi) {

    suspend fun commonSearch(keywords: String): DesignResponse<CombineSearchRes> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "SearchRepository, thread:${Thread.currentThread()}")
            val designResponse = searchApi.commonSearch(keywords)
            val combineSearchRes = designResponse.data
            if (!combineSearchRes?.applications.isNullOrEmpty()) {
                PictureUrlMapper.mapPictureUrl(combineSearchRes.applications)
            }
            if (!combineSearchRes?.users.isNullOrEmpty()) {
                PictureUrlMapper.mapPictureUrl(combineSearchRes.users)
            }
            if (!combineSearchRes?.groups.isNullOrEmpty()) {
                PictureUrlMapper.mapPictureUrl(combineSearchRes.groups)

            }
            if (!combineSearchRes?.screens.isNullOrEmpty()) {
                ScreenRepository.mapScreensIfNeeded(combineSearchRes.screens)
            }
            return@withContext designResponse
        }

    companion object {
        private const val TAG = "SearchRepository"

        private var INSTANCE: SearchRepository? = null

        fun getInstance(): SearchRepository {
            if (INSTANCE == null) {
                val api = ApiProvider.provide(SearchApi::class.java)
                val repository = SearchRepository(api)
                INSTANCE = repository
            }
            return INSTANCE!!
        }
    }
}