package xcj.app.web.webserver.netty

import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.QueryStringDecoder

data class HttpRequestWrapper(
    val httpRequest: HttpRequest,
    val queryStringDecoder: QueryStringDecoder,
    val port: Int
)