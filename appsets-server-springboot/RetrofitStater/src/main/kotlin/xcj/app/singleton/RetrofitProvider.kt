package xcj.app.singleton

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitProvider {
    private val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    private val mOkHttpClient: OkHttpClient = getOkHttpClient()

    private val mRetrofitBuilder: Retrofit.Builder = getRetrofitBuilder()

    private fun getRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder()
            .client(mOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
    }


    private fun getOkHttpClient(): OkHttpClient{
        return OkHttpClient.Builder().apply {
           /* val loggingBody = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val loggingHeader = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }*/
            //val headerInterceptor = HeaderInterceptor(LoginUserManager.token)
            //设置缓存配置 缓存最大10M
            //cache(Cache(File(appContext.cacheDir, "cxk_cache"), 10 * 1024 * 1024))
            //添加Cookies自动持久化
            //cookieJar(cookieJar)
            //添加公共heads 注意要设置在日志拦截器之前，不然Log中会不显示head信息
            //addInterceptor(loggingHeader)
            //添加缓存拦截器 可传入缓存天数，不传默认7天
            //addInterceptor(loggingBody)
            // 日志拦截器
            //addInterceptor(headerInterceptor)
            //超时时间 连接、读、写
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(5, TimeUnit.SECONDS)
            writeTimeout(5, TimeUnit.SECONDS)
        }.build()
    }

    fun <T> getService(baseUrl: String, clazz: Class<T>):T{
        return mRetrofitBuilder.baseUrl(baseUrl).build().create(clazz) as T
    }

    class HeaderInterceptor(
        private val token:String,
        private val clientFlag:String
    ):okhttp3.Interceptor{
        override fun intercept(chain: Interceptor.Chain): Response {
            val newRequest = chain.request()
                .newBuilder()
                .header("token", token)
                .header("client-flag", clientFlag)
                .build()
            return chain.proceed(newRequest)
        }
    }
}

