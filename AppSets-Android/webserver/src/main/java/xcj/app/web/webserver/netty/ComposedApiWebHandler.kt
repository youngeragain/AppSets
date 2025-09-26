package xcj.app.web.webserver.netty

import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.EmptyByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpObject
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.ContentUploadN
import xcj.app.web.webserver.interfaces.ComponentsProvider
import xcj.app.web.webserver.interfaces.ListenersProvider

class ComposedApiWebHandler(
    private val port: Int,
    private val handlerMappingCache: List<HandlerMapping>,
    private val componentsProvider: ComponentsProvider?,
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
            if (!HttpUtil.isContentLengthSet(httpResponse) && httpResponse is FullHttpResponse) {
                val length = httpResponse.content().writerIndex()
                httpResponse.headers().set(HttpHeaderNames.CONTENT_LENGTH, length)
            }
            var channelFuture = ctx.write(httpResponse)
            channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
            if (!HttpUtil.isKeepAlive(httpRequest)) {
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
            return
        }
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

    private fun handleFullHttpRequest(
        ctx: ChannelHandlerContext,
        httpRequest: FullHttpRequest
    ) {
        PurpleLogger.current.d(TAG, "handleFullHttpRequest")
        if (HttpUtil.is100ContinueExpected(httpRequest)) {
            ComposedApiWebHandler.send100Continue(ctx)
        }
        val uri = httpRequest.uri()
        val queryStringDecoder = QueryStringDecoder(uri)

        val response = DefaultFullHttpResponse(
            HttpVersion.HTTP_1_1,
            HttpResponseStatus.OK,
            EmptyByteBuf(ByteBufAllocator.DEFAULT)
        )

        val httpRequestWrapper = HttpRequestWrapper(httpRequest, queryStringDecoder, port)
        val httpResponseWrapper = HttpResponseWrapper(response)
        val handlerMapping = getHandlerMapping(ctx, httpRequestWrapper)
        if (handlerMapping == null) {
            responseNormal(ctx, httpRequestWrapper.httpRequest, notFoundUriResponse)
            return
        }

        try {
            handlerMapping.handle(
                ctx,
                httpRequestWrapper,
                httpResponseWrapper,
                componentsProvider,
                listenersProvider
            )
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
            ComposedApiWebHandler.sendBadRequest(ctx)
            return
        }
        //httpRequestWrapper.httpContent = content
        try {
            //handlerMapping.handle(ctx, httpRequestWrapper, httpResponseWrapper, listenersProvider)
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.e(TAG, "handleLastHttpContent, exception:${e.message}")
            ComposedApiWebHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                ComposedApiWebHandler.serverInternalErrorResponse
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

    /**
     * @param content maybe LastHttContent
     */
    private fun handleHttpContent(
        ctx: ChannelHandlerContext,
        content: HttpContent
    ) {
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
            handlerMapping.handle(
                ctx,
                httpRequestWrapper,
                httpResponseWrapper,
                componentsProvider,
                listenersProvider
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.e(TAG, "handleHttpContent, exception:${e.message}")
            ComposedApiWebHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                ComposedApiWebHandler.serverInternalErrorResponse
            )
        }
    }

    private fun handleHttpRequest(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequest
    ) {
        PurpleLogger.current.d(TAG, "handleHttpRequest")
        if (HttpUtil.is100ContinueExpected(httpRequest)) {
            ComposedApiWebHandler.send100Continue(ctx)
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
            ComposedApiWebHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                ComposedApiWebHandler.notFoundUriResponse
            )
            return
        }
        try {

            val defaultHttpDataFactory = DefaultHttpDataFactory(true)

            val decoder = HttpPostRequestDecoder(defaultHttpDataFactory, httpRequest)
            httpRequestWrapper.httpPostRequestDecoder = decoder
            if (decoder.isMultipart) {
                val shareDirPath = componentsProvider?.provideShareDirPath()
                defaultHttpDataFactory.setBaseDir(shareDirPath)

                val contentUploadN = ContentUploadN()

                httpRequestWrapper.contentUploadN = contentUploadN
                if (HttpUtil.isContentLengthSet(httpRequest)) {
                    val length =
                        httpRequest.headers().get(HttpHeaderNames.CONTENT_LENGTH).toLongOrNull()
                            ?: 0
                    contentUploadN.total = length

                }
            }

            this.handlerMapping = handlerMapping
            this.httpRequestWrapper = httpRequestWrapper
            this.httpResponseWrapper = httpResponseWrapper

        } catch (e: HttpPostRequestDecoder.ErrorDataDecoderException) {
            e.printStackTrace()
            ComposedApiWebHandler.sendBadRequest(ctx)
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