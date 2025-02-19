package xcj.app.web.webserver.netty

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType

class WebHandler(
    private val port: Int,
    private val handlerMappingCache: List<HandlerMapping>,
) : SimpleChannelInboundHandler<HttpObject>() {

    companion object {
        private const val TAG = "WebHandler"
    }

    private val serverInternalErrorResponse = DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.INTERNAL_SERVER_ERROR,
        Unpooled.wrappedBuffer("Server internal error".toByteArray())
    ).apply {
        headers().set(HttpHeaderNames.CONTENT_TYPE, ContentType.TEXT_PLAIN)
    }
    private val notFoundUriResponse = DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK,
        Unpooled.wrappedBuffer("Not found".toByteArray())
    ).apply {
        headers().set(HttpHeaderNames.CONTENT_TYPE, ContentType.TEXT_PLAIN)
    }

    private fun responseNormal(ctx: ChannelHandlerContext, httpResponse: HttpResponse) {
        ctx.channel().writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE)
    }

    private fun getHandlerMapping(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequestWrapper
    ): HandlerMapping? {
        if (httpRequest.httpRequest.uri().isNullOrEmpty()) {
            return null
        }
        for (handlerMapping in handlerMappingCache) {
            if (handlerMapping.isSupport(httpRequest)) {
                return handlerMapping
            }
        }
        return null
    }

    private fun handleFullHttpRequest(
        ctx: ChannelHandlerContext,
        fullHttpRequest: FullHttpRequest
    ) {
        val uri = fullHttpRequest.uri()
        if (uri == "/favicon.ico") {
            PurpleLogger.current.d(
                TAG,
                "handleFullHttpRequest, uri is favicon.ico"
            )
            return
        }
        if (fullHttpRequest.toString() == "EmptyLastHttpContent") {
            PurpleLogger.current.d(
                TAG,
                "handleFullHttpRequest, fullHttpRequest is EmptyLastHttpContent"
            )
            return
        }
        val queryStringDecoder = QueryStringDecoder(uri)

        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(byteArrayOf())
        )

        val httpRequestWrapper = HttpRequestWrapper(fullHttpRequest, queryStringDecoder, port)
        val httpResponseWrapper = HttpResponseWrapper(response)
        val handlerMapping = getHandlerMapping(ctx, httpRequestWrapper)
        if (handlerMapping == null) {
            responseNormal(ctx, notFoundUriResponse)
            return
        }

        try {
            val handleResult =
                handlerMapping.handle(ctx, httpRequestWrapper, httpResponseWrapper)
            if (handleResult == HandleResult.EMPTY) {
                PurpleLogger.current.w(
                    TAG,
                    "handleFullHttpRequest, handlerMethod hand result is empty!"
                )
                responseNormal(ctx, notFoundUriResponse)
            } else {
                val handleHttpResponse = handleResult.getResult()
                if (handleHttpResponse != null) {
                    responseNormal(ctx, handleHttpResponse)
                } else {
                    responseNormal(ctx, serverInternalErrorResponse)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.e(TAG, "handleFullHttpRequest, exception:${e}")
            responseNormal(ctx, serverInternalErrorResponse)
        }
    }

    private fun terminateTransmissionToNextHandlerIfNeeded(
        ctx: ChannelHandlerContext,
        method: HandlerMethod
    ) {

        if (method.isHanlePostFileUri()) {
            PurpleLogger.current.d(
                TAG,
                "terminateTransmissionToNextHandlerIfNeeded, method:$method, true"
            )
            ctx.fireChannelReadComplete()
        } else {
            PurpleLogger.current.d(
                TAG,
                "terminateTransmissionToNextHandlerIfNeeded, method:$method, false"
            )
        }
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        PurpleLogger.current.d(TAG, "channelRead0")
        if (msg is FullHttpRequest) {
            handleFullHttpRequest(ctx, msg)
        }
        if (msg is LastHttpContent) {

        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        super.exceptionCaught(ctx, cause)
        PurpleLogger.current.d(TAG, "exceptionCaught, cause：$cause")
    }

    override fun channelRegistered(ctx: ChannelHandlerContext?) {
        super.channelRegistered(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        super.channelUnregistered(ctx)
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        PurpleLogger.current.d(TAG, "channelInactive")
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        super.channelReadComplete(ctx)
        //ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
        //.addListener(ChannelFutureListener.CLOSE)
        PurpleLogger.current.d(TAG, "channelReadComplete")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        super.userEventTriggered(ctx, evt)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
        super.channelWritabilityChanged(ctx)
    }
}