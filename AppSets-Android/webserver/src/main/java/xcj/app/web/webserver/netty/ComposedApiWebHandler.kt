package xcj.app.web.webserver.netty

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.ShareSystem
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.FileUploadN
import xcj.app.web.webserver.interfaces.ListenersProvider

class ComposedApiWebHandler(
    private val port: Int,
    private val handlerMappingCache: List<HandlerMapping>,
    private val listenersProvider: ListenersProvider?
) : SimpleChannelInboundHandler<HttpObject>() {

    companion object {
        private const val TAG = "ComposedApiWebHandler"

        val serverInternalErrorResponse: HttpResponse
            get() = DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                Unpooled.wrappedBuffer("Server internal error".toByteArray())
            ).apply {
                headers().set(HttpHeaderNames.CONTENT_TYPE, ContentType.TEXT_PLAIN)
            }
        val notFoundUriResponse: HttpResponse
            get() = DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("Not found".toByteArray())
            ).apply {
                headers().set(HttpHeaderNames.CONTENT_TYPE, ContentType.TEXT_PLAIN)
            }

        val continueResponse: HttpResponse
            get() = DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE)

        val badRequestResponse: HttpResponse
            get() = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST)

        fun send100Continue(ctx: ChannelHandlerContext) {
            PurpleLogger.current.d(TAG, "send100Continue")
            ctx.writeAndFlush(continueResponse)
        }

        fun sendBadRequest(ctx: ChannelHandlerContext) {
            PurpleLogger.current.d(TAG, "sendBadRequest")
            ctx.channel().writeAndFlush(badRequestResponse).addListener(ChannelFutureListener.CLOSE)
        }

        fun responseNormal(
            ctx: ChannelHandlerContext,
            httpRequest: HttpRequest,
            httpResponse: HttpResponse
        ) {
            PurpleLogger.current.d(TAG, "responseNormal, uri:${httpRequest.uri()}")
            var channelFuture = ctx.write(httpResponse)
            channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            if (!HttpUtil.isKeepAlive(httpRequest)) {
                channelFuture.addListener(ChannelFutureListener.CLOSE)
            } else {
                channelFuture.addListener(ChannelFutureListener.CLOSE)
            }
        }
    }

    private var handlerMapping: HandlerMapping? = null
    private var httpRequestWrapper: HttpRequestWrapper? = null
    private var httpResponseWrapper: HttpResponseWrapper? = null

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


    override fun channelRead0(ctx: ChannelHandlerContext, msg: HttpObject) {
        if (msg is FullHttpRequest) {
            handleFullHttpRequest(ctx, msg)
        } else {
            if (msg is HttpRequest) {
                handleHttpRequest(ctx, msg)
            }
            if (msg is HttpContent) {
                handleHttpContent(ctx, msg)
            }
            if (msg is LastHttpContent) {
                handleLastHttpContent(ctx, msg)
            }
        }
    }

    private fun handleFullHttpRequest(
        ctx: ChannelHandlerContext,
        fullHttpRequest: FullHttpRequest
    ) {
        PurpleLogger.current.d(TAG, "handleFullHttpRequest")
        val uri = fullHttpRequest.uri()
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
            responseNormal(ctx, httpRequestWrapper.httpRequest, notFoundUriResponse)
            return
        }

        try {
            handlerMapping.handle(ctx, httpRequestWrapper, httpResponseWrapper, listenersProvider)
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.e(TAG, "handleFullHttpRequest, exception:${e}")
            responseNormal(ctx, httpRequestWrapper.httpRequest, serverInternalErrorResponse)
        }
    }

    private fun handleLastHttpContent(
        ctx: ChannelHandlerContext,
        content: LastHttpContent
    ) {
        PurpleLogger.current.d(TAG, "handleLastHttpContent")
        val handlerMapping = handlerMapping
        if (handlerMapping == null) {
            return
        }
        val httpRequestWrapper = httpRequestWrapper
        if (httpRequestWrapper == null) {
            return
        }

        val httpResponseWrapper = httpResponseWrapper
        if (httpResponseWrapper == null) {
            return
        }
        if (!content.decoderResult().isSuccess) {
            sendBadRequest(ctx)
            return
        }
        httpRequestWrapper.lastHttpContent = content
        try {
            handlerMapping.handle(ctx, httpRequestWrapper, httpResponseWrapper, listenersProvider)
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.e(TAG, "handleLastHttpContent, exception:${e.message}")
            responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                serverInternalErrorResponse
            )
        } finally {
            reset()
        }
    }

    private fun reset() {
        PurpleLogger.current.d(TAG, "reset")
        httpRequestWrapper?.close()
        handlerMapping = null
        httpRequestWrapper = null
        httpResponseWrapper = null
    }

    private fun handleHttpContent(
        ctx: ChannelHandlerContext,
        content: HttpContent
    ) {
        PurpleLogger.current.d(TAG, "handleHttpContent")
        val handlerMapping = handlerMapping
        if (handlerMapping == null) {
            return
        }
        val httpRequestWrapper = httpRequestWrapper
        if (httpRequestWrapper == null) {
            return
        }

        val httpResponseWrapper = httpResponseWrapper
        if (httpResponseWrapper == null) {
            return
        }
        httpRequestWrapper.httpContent = content
        try {
            handlerMapping.handle(ctx, httpRequestWrapper, httpResponseWrapper, listenersProvider)
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.e(TAG, "handleHttpContent, exception:${e.message}")
            responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                serverInternalErrorResponse
            )
        }
    }

    private fun handleHttpRequest(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequest
    ) {
        PurpleLogger.current.d(TAG, "handleHttpRequest")
        if (HttpUtil.is100ContinueExpected(httpRequest)) {
            send100Continue(ctx)
        }
        val uri = httpRequest.uri()
        val queryStringDecoder = QueryStringDecoder(uri)

        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(byteArrayOf())
        )

        val httpRequestWrapper = HttpRequestWrapper(httpRequest, queryStringDecoder, port)
        val httpResponseWrapper = HttpResponseWrapper(response)
        val handlerMapping = getHandlerMapping(ctx, httpRequestWrapper)
        if (handlerMapping == null) {
            responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                notFoundUriResponse
            )
            return
        }
        try {
            val defaultHttpDataFactory = DefaultHttpDataFactory(true)
            val shareDirPath = ShareSystem.getShareDirPath()
            defaultHttpDataFactory.setBaseDir(shareDirPath)
            val decoder = HttpPostRequestDecoder(defaultHttpDataFactory, httpRequest)

            httpRequestWrapper.httpPostRequestDecoder = decoder
            if (HttpUtil.isContentLengthSet(httpRequest)) {
                val length =
                    httpRequest.headers().get(HttpHeaderNames.CONTENT_LENGTH).toLongOrNull() ?: 0
                httpRequestWrapper.fileUploadN = FileUploadN(length, 0)
            }

            this.handlerMapping = handlerMapping
            this.httpRequestWrapper = httpRequestWrapper
            this.httpResponseWrapper = httpResponseWrapper

        } catch (e: HttpPostRequestDecoder.ErrorDataDecoderException) {
            e.printStackTrace()
            sendBadRequest(ctx)
            return
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        super.exceptionCaught(ctx, cause)
        PurpleLogger.current.d(TAG, "exceptionCaught, cause：$cause")
        ctx.close()
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
        PurpleLogger.current.d(TAG, "channelReadComplete")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        super.userEventTriggered(ctx, evt)
    }

    override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
        super.channelWritabilityChanged(ctx)
    }
}