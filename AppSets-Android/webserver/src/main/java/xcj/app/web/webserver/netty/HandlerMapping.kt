package xcj.app.web.webserver.netty

import io.netty.channel.ChannelHandlerContext
import xcj.app.web.webserver.interfaces.ListenersProvider

interface HandlerMapping {

    fun handle(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        listenersProvider: ListenersProvider?
    )

    fun isSupport(httpRequest: HttpRequestWrapper): Boolean

    fun getHandlerMethod(httpRequest: HttpRequestWrapper): HandlerMethod?
}