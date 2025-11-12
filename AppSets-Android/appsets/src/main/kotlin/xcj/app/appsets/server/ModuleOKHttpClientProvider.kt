package xcj.app.appsets.server

import okhttp3.OkHttpClient
import xcj.app.appsets.server.interceptor.AddFixedHeaderInterceptor
import xcj.app.appsets.server.interceptor.BaseUrlInterceptor
import xcj.app.appsets.server.ssl.DesignHostnameVerifier
import xcj.app.appsets.server.ssl.DesignX509TrustManager
import xcj.app.starter.foundation.Provider
import xcj.app.starter.server.RetrofitProvider
import java.security.SecureRandom
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class ModuleOKHttpClientProvider : Provider<OkHttpClient> {
    private val mOkHttpClient: OkHttpClient by lazy {
        RetrofitProvider.defaultOkHttpClientBuilder().apply {
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
    }

    override fun provide(): OkHttpClient {
        return mOkHttpClient
    }
}