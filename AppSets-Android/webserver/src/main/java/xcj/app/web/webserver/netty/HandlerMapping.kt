package xcj.app.web.webserver.netty

import io.netty.channel.ChannelHandlerContext

interface HandlerMapping {

    fun handle(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequestWrapper,
        httpResponse: HttpResponseWrapper
    ): HandleResult

    fun isSupport(httpRequest: HttpRequestWrapper): Boolean

    fun getHandlerMethod(httpRequest: HttpRequestWrapper): HandlerMethod?
}