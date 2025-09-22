package xcj.app.starter.server

import okhttp3.OkHttpClient
import xcj.app.starter.android.util.PurpleLogger
import java.util.concurrent.TimeUnit

object RetrofitProvider {
    private const val TAG = "RetrofitProvider"
    private val retrofitMap: LinkedHashMap<String, retrofit2.Retrofit> =
        LinkedHashMap(2, 0.75f, true)

    private val mRetrofitBuilder: retrofit2.Retrofit.Builder
        get() = makeRetrofitBuilder()

    val defaultOkHttpClient: OkHttpClient
        get() = defaultOkHttpClientBuilder().build()

    private fun makeRetrofitBuilder(): retrofit2.Retrofit.Builder {
        PurpleLogger.current.d(TAG, "makeRetrofitBuilder")
        return retrofit2.Retrofit.Builder()
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
    }

    fun defaultOkHttpClientBuilder(): OkHttpClient.Builder {
        PurpleLogger.current.d(TAG, "defaultOkHttpClientBuilder")
        return OkHttpClient.Builder().apply {
            if (PurpleLogger.current.enable) {
                val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }
            callTimeout(20, TimeUnit.SECONDS)
            connectTimeout(20, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            writeTimeout(600, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            fastFallback(true)
        }
    }

    fun release() {
        retrofitMap.clear()
    }

    fun <T> getService(
        baseUrl: String,
        clazz: Class<T>,
        okHttpClient: OkHttpClient?,
        reuse: Boolean = true,
    ): T {
        if (!reuse) {
            val okHttpClientOverride = okHttpClient ?: defaultOkHttpClient
            val retrofit = mRetrofitBuilder.baseUrl(baseUrl).client(okHttpClientOverride).build()
            return retrofit.create(clazz) as T

        }
        return retrofitMap.getOrPut(baseUrl) {
            val okHttpClientOverride = okHttpClient ?: defaultOkHttpClient
            mRetrofitBuilder.baseUrl(baseUrl).client(okHttpClientOverride).build()
        }.create(clazz) as T
    }
}