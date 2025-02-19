package xcj.app.web.webserver.netty

import io.netty.handler.codec.http.HttpResponse

data class HttpResponseWrapper(val httpResponse: HttpResponse)