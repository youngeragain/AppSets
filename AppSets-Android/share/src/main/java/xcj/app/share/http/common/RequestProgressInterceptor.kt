package xcj.app.share.http.common

import okhttp3.Interceptor
import okhttp3.Response
import xcj.app.share.base.DataContent
import xcj.app.web.webserver.base.ProgressListener

class RequestProgressInterceptor(
    private val dataContent: DataContent,
    private val progressListener: ProgressListener?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request =
            originalRequest.newBuilder()
                .method(originalRequest.method, originalRequest.body)
                .build()
        return chain.proceed(request)

    }
}