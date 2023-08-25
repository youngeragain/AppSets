package xcj.app.userinfo.config

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import xcj.app.CoreLogger

class RestTemplateLoggInterceptor: ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        CoreLogger.d("blue", """
            request:
            ${request.method.name()}
            ${request.uri}
            headers
            ${request.headers}
            body:
            ${String(body)}
        """.trimIndent())
        val response = execution.execute(request, body)
        CoreLogger.d("blue", """
            response:
            ${request.method.name()}
            ${request.uri}
            headers
            ${response.headers}
            body:
            ${String(response.body.readAllBytes())}
        """.trimIndent())
        return response
    }
}