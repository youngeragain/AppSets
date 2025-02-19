package xcj.app.web.webserver.netty

import io.netty.channel.ChannelHandlerContext

class RequestPathHandlerMapping(
    val fixedUriHandlerMethodMap: Map<String, HandlerMethod>,
    val dynamicUriHandlerMethodMap: Map<String, HandlerMethod>
) : HandlerMapping {

    override fun handle(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequestWrapper,
        httpResponse: HttpResponseWrapper
    ): HandleResult {

        val handlerMethod = getHandlerMethod(httpRequest)
        if (handlerMethod == null) {
            return HandleResult.EMPTY
        }
        return DefaultHandleResult(handlerMethod, ctx, httpRequest, httpResponse)

    }

    override fun isSupport(httpRequest: HttpRequestWrapper): Boolean {
        return true
    }

    override fun getHandlerMethod(httpRequest: HttpRequestWrapper): HandlerMethod? {
        val uriHandlerMethodsMap = fixedUriHandlerMethodMap
        if (uriHandlerMethodsMap.isEmpty()) {
            return null
        }
        val handlerMethod = uriHandlerMethodsMap[httpRequest.queryStringDecoder.path()]
        return handlerMethod

    }
}