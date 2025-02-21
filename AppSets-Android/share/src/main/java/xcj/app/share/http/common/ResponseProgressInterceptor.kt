package xcj.app.share.http.common

import okhttp3.Interceptor
import okhttp3.Response
import xcj.app.share.base.DataContent
import xcj.app.web.webserver.base.ProgressListener

class ResponseProgressInterceptor(
    private val dataContent: DataContent,
    private val progressListener: ProgressListener?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val progressResponseBody =
            ProgressResponseBody(dataContent, originalResponse.body, progressListener)
        return originalResponse.newBuilder()
            .body(progressResponseBody)
            .build()
    }
}