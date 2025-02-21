package xcj.app.starter.server

import kotlinx.coroutines.Runnable
import okhttp3.OkHttpClient
import xcj.app.starter.android.util.PurpleLogger
import java.net.InetAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object RetrofitProvider {
    private const val TAG = "RetrofitProvider"
    private val mRetrofitBuilder: retrofit2.Retrofit.Builder = makeRetrofitBuilder()
    private val retrofitMap: LinkedHashMap<String, retrofit2.Retrofit> =
        LinkedHashMap(4, 0.75f, true)

    val defaultOkHttpClient: OkHttpClient = defaultOkHttpClientBuilder().build()

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
                    level = okhttp3.logging.HttpLoggingInterceptor.Level.HEADERS
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
        notReuse: Boolean = false
    ): T {
        if (notReuse) {
            var okHttpClientOverride = okHttpClient ?: defaultOkHttpClient
            val retrofit = mRetrofitBuilder.baseUrl(baseUrl).client(okHttpClientOverride).build()
            return retrofit.create(clazz) as T

        }
        return retrofitMap.getOrPut(baseUrl) {
            var okHttpClientOverride = okHttpClient ?: defaultOkHttpClient
            mRetrofitBuilder.baseUrl(baseUrl).client(okHttpClientOverride).build()
        }.create(clazz) as T
    }

    private class DesignDNS : okhttp3.Dns, Runnable {

        companion object {
            private const val TAG = "DesignDNS"
        }

        private var hostname: String? = null
        private var results: String? = null
        private val mPingExecutor: ExecutorService = Executors.newSingleThreadExecutor()

        override fun run() {
            PurpleLogger.current.d(TAG, "run ping host for host:$hostname")
            if (hostname.isNullOrEmpty()) {
                results = "Unreachable"
                return
            }
            val process = Runtime.getRuntime().exec("ping $hostname")
            val ins = process.inputStream.bufferedReader()
            ins.readLine()//first line is cmd
            results = ins.readLine()
        }

        override fun lookup(hostname: String): List<InetAddress> {

            this.hostname = hostname
            mPingExecutor.execute(this)
            val startTimeMills = System.currentTimeMillis()
            while (true) {
                if (results?.contains("Unreachable") == true) {
                    PurpleLogger.current.d(TAG, "lookup, hostname:${hostname} unreachable!")
                    return emptyList()
                }
                if (results.isNullOrEmpty()) {
                    val interval = System.currentTimeMillis() - startTimeMills
                    if (interval > 1000) {
                        PurpleLogger.current.d(TAG, "lookup, hostname:${hostname} unreachable!")
                        return emptyList()
                    } else {
                        continue
                    }
                }
                break
            }
            PurpleLogger.current.d(TAG, "lookup, hostname:${hostname} get by default!")
            return InetAddress.getAllByName(hostname).toList()
        }
    }
}