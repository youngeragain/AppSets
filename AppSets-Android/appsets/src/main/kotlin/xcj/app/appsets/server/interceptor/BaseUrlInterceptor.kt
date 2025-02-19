package xcj.app.appsets.server.interceptor

import okhttp3.HttpUrl.Companion.toHttpUrl
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.starter.android.util.PurpleLogger

class BaseUrlInterceptor : okhttp3.Interceptor {
    companion object {
        private const val TAG = "BaseUrlInterceptor"
    }

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        PurpleLogger.current.d(TAG, "intercept")
        val rawRequest = chain.request()
        try {
            val rawFullUrl = rawRequest.url.toString()
            val rawOnlyBaseUrl = rawRequest.url.newBuilder("/")?.build().toString()
            val urlSuffix = rawFullUrl.substring(rawOnlyBaseUrl.length)
            val replacedUrl = "${
                ApiProvider.urlHook(
                    rawOnlyBaseUrl,
                    rawFullUrl
                )
            }$urlSuffix".toHttpUrl()
            val newRequestBuilder = rawRequest.newBuilder().url(replacedUrl)
            val newRequest = newRequestBuilder.build()
            return chain.proceed(newRequest)
        } catch (e: IllegalArgumentException) {
            PurpleLogger.current.d(TAG, "intercept exception:${e.message}")
        }
        return chain.proceed(rawRequest)
    }
}