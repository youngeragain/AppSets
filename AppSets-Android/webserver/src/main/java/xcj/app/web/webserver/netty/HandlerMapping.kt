package xcj.app.web.webserver.netty

import io.netty.channel.ChannelHandlerContext

interface HandlerMapping {

    fun handle(
        webHandler: WebHandler,
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper
    )

    fun isSupport(httpRequest: HttpRequestWrapper): Boolean

    fun getHandlerMethod(httpRequest: HttpRequestWrapper): HandlerMethod?
}