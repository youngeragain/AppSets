package xcj.app.appsets.server.api

import xcj.app.appsets.server.ModuleOKHttpClientProvider
import xcj.app.appsets.settings.ModuleConfig
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.server.RetrofitProvider
import java.lang.reflect.Proxy

object ApiProvider {
    private const val TAG = "ApiProvider"

    private const val IS_ANDROID_STUDIO_PREVIEW = false

    private fun <U> makeBaseUrl(apiClazz: Class<U>): String {
        val url = if (ModuleConfig.moduleConfiguration.apiUrl.isEmpty()) {
            "${ModuleConfig.moduleConfiguration.apiSchema}://${ModuleConfig.moduleConfiguration.apiHost}:${ModuleConfig.moduleConfiguration.apiPort}/"
        } else {
            "${ModuleConfig.moduleConfiguration.apiSchema}://${ModuleConfig.moduleConfiguration.apiUrl}"
        }
        return when (apiClazz) {
            UserApi::class.java -> {
                url
            }

            AppSetsApi::class.java -> {
                url
            }

            QRCodeApi::class.java -> {
                url
            }

            SearchApi::class.java -> {
                url
            }

            ThirdPartApi1::class.java -> {
                url
            }

            else -> throw Exception("can't provide an api")
        }
    }

    fun urlHook(rawBaseUrl: String, rawFullUrl: String): String {
        return rawBaseUrl
    }

    fun <U> provide(apiClazz: Class<U>): U {
        if (IS_ANDROID_STUDIO_PREVIEW) {
            return innerProvideForAndroidStudioPreview(apiClazz)
        }
        return innerProvideForRuntime(apiClazz)
    }

    private var okHttpClientProvider: ModuleOKHttpClientProvider? = null

    private fun <U> innerProvideForRuntime(apiClazz: Class<U>): U {
        if (okHttpClientProvider == null) {
            okHttpClientProvider = ModuleOKHttpClientProvider()
        }
        return RetrofitProvider.getService(
            baseUrl = makeBaseUrl(apiClazz),
            clazz = apiClazz,
            okHttpClientProvider = okHttpClientProvider
        )
    }

    private fun <U> innerProvideForAndroidStudioPreview(apiClazz: Class<U>): U {
        return Proxy.newProxyInstance(
            apiClazz.classLoader,
            arrayOf(apiClazz)
        ) { proxy, method, args ->
            PurpleLogger.current.d(
                TAG,
                "innerProvideForAndroidStudioPreview, proxy:$proxy, method:${method.name}"
            )
            DesignResponse.NO_DATA
        } as U
    }
}