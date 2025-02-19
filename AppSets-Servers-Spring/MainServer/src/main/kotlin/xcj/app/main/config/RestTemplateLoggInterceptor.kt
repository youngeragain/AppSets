package xcj.app.main.config

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import xcj.app.util.PurpleLogger

class RestTemplateLoggInterceptor : ClientHttpRequestInterceptor {

    companion object {
        private const val TAG = "RestTemplateLoggInterceptor"
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        PurpleLogger.current.d(
            TAG, """
            request:
            ${request.method.name()}
            ${request.uri}
            headers
            ${request.headers}
            body:
            ${String(body)}
        """.trimIndent()
        )
        val response = execution.execute(request, body)
        PurpleLogger.current.d(
            TAG, """
            response:
            ${request.method.name()}
            ${request.uri}
            headers
            ${response.headers}
            body:
            ${String(response.body.readAllBytes())}
        """.trimIndent()
        )
        return response
    }
}