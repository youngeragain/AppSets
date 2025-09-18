package xcj.app.appsets.server.api

import xcj.app.appsets.server.interceptor.AddFixedHeaderInterceptor
import xcj.app.appsets.server.interceptor.BaseUrlInterceptor
import xcj.app.appsets.server.ssl.DesignHostnameVerifier
import xcj.app.appsets.server.ssl.DesignX509TrustManager
import xcj.app.appsets.settings.ModuleConfig
import xcj.app.starter.server.RetrofitProvider
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object ApiProvider {

    private val mOkHttpClient = RetrofitProvider.defaultOkHttpClientBuilder().apply {
        val sslContext = SSLContext.getInstance("SSL")
        val x509TrustManager: X509TrustManager = DesignX509TrustManager()
        val hostnameVerifier: HostnameVerifier = DesignHostnameVerifier()
        sslContext.init(null, arrayOf(x509TrustManager), SecureRandom())
        sslSocketFactory(sslContext.socketFactory, x509TrustManager)
        hostnameVerifier(hostnameVerifier)
        val baseUrlInterceptor = BaseUrlInterceptor()
        val addFixedHeaderInterceptor = AddFixedHeaderInterceptor()
        addInterceptor(addFixedHeaderInterceptor)
        addInterceptor(baseUrlInterceptor)
    }.build()

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
        return RetrofitProvider.getService(makeBaseUrl(apiClazz), apiClazz, mOkHttpClient)
    }
}