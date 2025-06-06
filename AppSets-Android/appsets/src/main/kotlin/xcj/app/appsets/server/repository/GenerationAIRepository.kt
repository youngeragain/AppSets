package xcj.app.appsets.server.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.server.api.AppSetsApi
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse

class GenerationAIRepository(private val appSetsApi: AppSetsApi) {

    suspend fun getGenerateContentWithNoneContext(): DesignResponse<String> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(
                TAG,
                "getGenerateContentWithNoneContext, thread:${Thread.currentThread()}"
            )
            return@withContext appSetsApi.getGenerateContentWithNoneContext()
        }

    companion object {

        private const val TAG = "GenerationAIContentRepository"

        fun newInstance(): GenerationAIRepository {
            val api = ApiProvider.provide(AppSetsApi::class.java)
            val repository = GenerationAIRepository(api)
            return repository
        }
    }
}