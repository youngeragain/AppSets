package xcj.app.share.http.common

import okhttp3.Interceptor
import okhttp3.Response
import xcj.app.share.base.DataContent
import xcj.app.web.webserver.interfaces.ProgressListener

class RequestProgressInterceptor(
    private val dataContent: DataContent,
    private val progressListener: ProgressListener?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBody = originalRequest.body
        if (requestBody == null) {
            return chain.proceed(originalRequest)
        }
        if (requestBody is ProgressRequestBody) {
            return chain.proceed(originalRequest)
        }
        val progressRequestBody = ProgressRequestBody(requestBody, dataContent, progressListener)
        val request =
            originalRequest.newBuilder()
                .method(originalRequest.method, progressRequestBody)
                .build()
        return chain.proceed(request)

    }
}