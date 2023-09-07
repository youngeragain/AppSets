package xcj.app.appsets.server

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.internal.tls.OkHostnameVerifier
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import xcj.app.appsets.BuildConfig
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.api.URLApi.Companion.urlHook
import xcj.app.appsets.server.repository.AppSetsRepository
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

object RetrofitProvider {
    private var mOkHttpClient: OkHttpClient? = null
    private var mRetrofitBuilder: Retrofit.Builder? = null
    private var baseUrlToRetrofit: LinkedHashMap<String, Retrofit>? = null
    private fun getRetrofitBuilder(): Retrofit.Builder {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        if (mOkHttpClient == null)
            mOkHttpClient = getOkHttpClient()
        return Retrofit.Builder()
            .client(mOkHttpClient!!)
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
    }

    private fun getOkHttpClient(): OkHttpClient{
        return OkHttpClient.Builder().apply {
            val sslContext = SSLContext.getInstance("SSL")
            val x509TrustManager: X509TrustManager = object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {

                }

                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {

                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
            val hostnameVerifier: HostnameVerifier =
                HostnameVerifier { hostname, session ->
                    Log.i("RetrofitProvider", "hostnameVerifier:${hostname}")
                    if (hostname == BuildConfig.ApiHostAddress) {
                        return@HostnameVerifier true
                    }
                    return@HostnameVerifier OkHostnameVerifier.verify(hostname, session)
                }
            sslContext.init(null, arrayOf(x509TrustManager), SecureRandom())

            sslSocketFactory(sslContext.socketFactory, x509TrustManager)
            hostnameVerifier(hostnameVerifier)
            val loggingBody = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val loggingHeader = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            val baseUrlInterceptor = BaseUrlInterceptor()
            val addFixedHeaderInterceptor = AddFixedHeaderInterceptor()
            //设置缓存配置 缓存最大10M
            //cache(Cache(File(appContext.cacheDir, "cxk_cache"), 10 * 1024 * 1024))
            //添加Cookies自动持久化
            //cookieJar(cookieJar)
            addInterceptor(addFixedHeaderInterceptor)
            addInterceptor(baseUrlInterceptor)
            //添加公共heads 注意要设置在日志拦截器之前，不然Log中会不显示head信息
            addInterceptor(loggingHeader)
            //添加缓存拦截器 可传入缓存天数，不传默认7天
            addInterceptor(loggingBody)
            // 日志拦截器
            //超时时间 连接、读、写
            callTimeout(200, TimeUnit.SECONDS)
            connectTimeout(200, TimeUnit.SECONDS)
            readTimeout(200, TimeUnit.SECONDS)
            writeTimeout(200, TimeUnit.SECONDS)
            retryOnConnectionFailure(false)
            /*       dns(object : Dns {
                       private val mPingExecutor: ExecutorService = Executors.newSingleThreadExecutor()
                       override fun lookup(hostname: String): List<InetAddress> {
                           val mPingRunnable: PingPing = PingPing()
                           mPingRunnable.hostname = hostname
                           mPingExecutor.execute(mPingRunnable)
                           val startTimeMills = System.currentTimeMillis()
                           while (true) {
                               if (mPingRunnable.results?.contains("Unreachable") == true) {
                                   Log.e("Dns", "hostname:${hostname} unreachable!")
                                   return emptyList()
                               }
                               if (mPingRunnable.results.isNullOrEmpty()) {
                                   val interval = System.currentTimeMillis() - startTimeMills
                                   if (interval > 1000) {
                                       Log.e("Dns", "hostname:${hostname} unreachable!")
                                       return emptyList()
                                   } else {
                                       continue
                                   }
                               }
                               break
                           }
                           Log.e("Dns", "hostname:${hostname} get by default!!!")
                           return InetAddress.getAllByName(hostname).toList()
                       }
                   })*/
        }.build()
    }

    class PingPing : Runnable {
        var hostname: String? = null
        var results: String? = null
        override fun run() {
            if (hostname.isNullOrEmpty()) {
                results = "Unreachable"
                return
            }
            val process = Runtime.getRuntime().exec("ping $hostname")
            val ins = process.inputStream.bufferedReader()
            ins.readLine()//first line is cmd
            results = ins.readLine()
        }
    }

    fun release() {
        mOkHttpClient = null
        mRetrofitBuilder = null
        baseUrlToRetrofit?.clear()
        baseUrlToRetrofit = null
    }

    fun <T> getService(baseUrl: String, clazz: Class<T>): T {
        if (baseUrlToRetrofit == null)
            baseUrlToRetrofit = LinkedHashMap(4, 0.75f, true)
        if (baseUrlToRetrofit!!.containsKey(baseUrl))
            return baseUrlToRetrofit!![baseUrl]!!.create(clazz) as T
        if (mRetrofitBuilder == null)
            mRetrofitBuilder = getRetrofitBuilder()
        val retrofit = mRetrofitBuilder!!.baseUrl(baseUrl).build()
        val apiInstance = retrofit.create(clazz) as T
        baseUrlToRetrofit!![baseUrl] = retrofit
        return apiInstance
    }

    class BaseUrlInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val rawRequest = chain.request()
            try {
                Log.e("BaseUrlInterceptor", "request tag:${rawRequest.tag()}")
                val rawFullUrl = rawRequest.url.toString()
                val rawOnlyBaseUrl = rawRequest.url.newBuilder("/")?.build().toString()
                val urlSuffix = rawFullUrl.substring(rawOnlyBaseUrl.length)
                val replacedUrl = "${urlHook(rawOnlyBaseUrl, rawFullUrl)}$urlSuffix".toHttpUrl()
                val newRequestBuilder = rawRequest.newBuilder().url(replacedUrl)
                val newRequest = newRequestBuilder.build()
                return chain.proceed(newRequest)
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
            return chain.proceed(rawRequest)
        }
    }
    class AddFixedHeaderInterceptor:Interceptor{
        override fun intercept(chain: Interceptor.Chain): Response {
            try {
                val requestBuilder = chain.request()
                    .newBuilder()
                    .header(ApiDesignEncodeStr.versionStrToMd5, "300")
                    .header(ApiDesignEncodeStr.platformStrToMd5, "android")
                if (!LocalAccountManager.token.isNullOrEmpty()) {
                    requestBuilder.header(
                        ApiDesignEncodeStr.tokenStrToMd5,
                        LocalAccountManager.token!!
                    )
                }
                val appToken = AppSetsRepository.getInstance().provideAppToken()
                if (!appToken.isNullOrEmpty()) {
                    requestBuilder.header(ApiDesignEncodeStr.appTokenStrToMd5, appToken)
                }
                val newRequest = requestBuilder.build()
                return chain.proceed(newRequest)
            }catch (e:IllegalArgumentException){
                e.printStackTrace()
            }
            return chain.proceed(chain.request())
        }
    }
}