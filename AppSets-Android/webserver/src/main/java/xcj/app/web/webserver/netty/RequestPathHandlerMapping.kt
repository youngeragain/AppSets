package xcj.app.web.webserver.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.EmptyByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelProgressiveFuture
import io.netty.channel.ChannelProgressiveFutureListener
import io.netty.channel.DefaultFileRegion
import io.netty.handler.codec.http.DefaultFullHttpRequest
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.EmptyHttpHeaders
import io.netty.handler.codec.http.HttpChunkedInput
import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.stream.ChunkedInput
import io.netty.handler.stream.ChunkedStream
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.ContentDownloadN
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.base.InputStreamReadableData
import xcj.app.web.webserver.interfaces.ProgressListener
import xcj.app.web.webserver.interfaces.ListenersProvider
import java.io.Closeable
import java.io.File
import java.lang.reflect.ParameterizedType
import java.util.UUID

class RequestPathHandlerMapping(
    val fixedUriHandlerMethodMap: Map<String, HandlerMethod>,
    val dynamicUriHandlerMethodMap: Map<String, HandlerMethod>
) : HandlerMapping {
    companion object {
        private const val TAG = "RequestPathHandlerMapping"
    }

    override fun handle(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        listenersProvider: ListenersProvider?
    ) {
        val handlerMethod = getHandlerMethod(httpRequestWrapper)
        if (handlerMethod == null) {
            ComposedApiWebHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                ComposedApiWebHandler.notFoundUriResponse
            )
            return
        }

        handleInternal(
            ctx,
            httpRequestWrapper,
            httpResponseWrapper,
            handlerMethod,
            listenersProvider
        )
    }

    private fun handleInternal(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        handlerMethod: HandlerMethod,
        listenersProvider: ListenersProvider?
    ) {
        val httpContent = httpRequestWrapper.httpContent
        if (httpContent != null) {
            handleFileApiInternal(
                ctx,
                httpRequestWrapper,
                httpResponseWrapper,
                handlerMethod,
                listenersProvider
            )
        } else {
            handleApiInternal(
                ctx,
                httpRequestWrapper,
                httpResponseWrapper,
                handlerMethod,
                listenersProvider
            )
        }
    }

    private fun handleFileApiInternal(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        handlerMethod: HandlerMethod,
        listenersProvider: ListenersProvider?
    ) {
        HttpFileUploadHelper.handleHttpContent(ctx, httpRequestWrapper, listenersProvider)
        val httpContent = httpRequestWrapper.httpContent
        if (httpContent == null) {
            return
        }
        if (httpContent !is LastHttpContent) {
            return
        }
        PurpleLogger.current.d(
            TAG,
            "handleFileApiInternal, handle LastHttpContent, uri:${httpRequestWrapper.httpRequest.uri()}"
        )

        val httpPostRequestDecoder = httpRequestWrapper.httpPostRequestDecoder
        if (httpPostRequestDecoder != null) {
            val currentPartialHttpData =
                httpPostRequestDecoder.currentPartialHttpData()
            if (currentPartialHttpData != null) {
                val rawHttpRequest = httpRequestWrapper.httpRequest
                val defaultFullHttpRequest = DefaultFullHttpRequest(
                    rawHttpRequest.protocolVersion(),
                    rawHttpRequest.method(),
                    rawHttpRequest.uri(),
                    EmptyByteBuf(ByteBufAllocator.DEFAULT),
                    rawHttpRequest.headers(), EmptyHttpHeaders.INSTANCE
                )
                //defaultFullHttpRequest.replace()
                httpRequestWrapper.httpRequest = defaultFullHttpRequest
            }
        }

        val returnValue = handlerMethod.call(ctx, httpRequestWrapper, httpResponseWrapper)
        handleMethodReturn(
            ctx,
            handlerMethod,
            httpRequestWrapper,
            httpResponseWrapper,
            returnValue,
            listenersProvider
        )
    }

    private fun handleApiInternal(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        handlerMethod: HandlerMethod,
        listenersProvider: ListenersProvider?
    ) {
        val response = httpResponseWrapper.httpResponse
        if (response !is DefaultFullHttpResponse) {
            return
        }

        val returnValue = handlerMethod.call(ctx, httpRequestWrapper, httpResponseWrapper)

        handleMethodReturn(
            ctx,
            handlerMethod,
            httpRequestWrapper,
            httpResponseWrapper,
            returnValue,
            listenersProvider
        )
    }

    private fun handleMethodReturn(
        ctx: ChannelHandlerContext,
        handlerMethod: HandlerMethod,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        returnValue: Any?,
        listenersProvider: ListenersProvider?
    ) {
        PurpleLogger.current.d(
            TAG,
            "handleMethodReturn, uri:${httpRequestWrapper.httpRequest.uri()}"
        )
        var returnValueOverride: Any? = returnValue
        val response = httpResponseWrapper.httpResponse
        if (response !is DefaultFullHttpResponse) {
            return
        }
        val httpHeaders = response.headers()

        if (HttpUtil.isKeepAlive(httpRequestWrapper.httpRequest)) {
            HttpUtil.setKeepAlive(response, true)
        }
        val guessContentType = getContentType(handlerMethod)
        if (!guessContentType.isNullOrEmpty()) {
            httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, guessContentType)
        }
        if (returnValue is Exception) {
            PurpleLogger.current.d(
                TAG,
                "handleMethodReturn, returnValue is Exception:${returnValue.message}"
            )
            returnValueOverride = DesignResponse(info = returnValue.message, data = null, code = -1)
            val bytes = handlerMethod.jsonTransformer.toString(returnValueOverride)
                .toByteArray()
            val responseBody = Unpooled.wrappedBuffer(bytes)
            val newResponse = response.replace(responseBody)
            ComposedApiWebHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                newResponse
            )
            return
        }

        if (returnValueOverride == null) {
            httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, 0)
            val responseBody = EmptyByteBuf(ByteBufAllocator.DEFAULT)
            val newResponse = response.replace(responseBody)
            ComposedApiWebHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
            return
        }

        when (handlerMethod.returnType) {
            Byte::class.java,
            Short::class.java,
            Int::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            String::class.java,
            Enum::class.java,
                -> {
                val bytes = returnValueOverride.toString().toByteArray()
                httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, bytes.size)
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                ComposedApiWebHandler.responseNormal(
                    ctx,
                    httpRequestWrapper.httpRequest,
                    newResponse
                )
            }

            Annotation::class.java,
            Nothing::class.java,
            Unit::class.java,
            Void::class.java -> {
                httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, 0)
                val responseBody = EmptyByteBuf(ByteBufAllocator.DEFAULT)
                val newResponse = response.replace(responseBody)
                ComposedApiWebHandler.responseNormal(
                    ctx,
                    httpRequestWrapper.httpRequest,
                    newResponse
                )
            }

            File::class.java -> {
                val file = returnValueOverride as File
                handleFileContentResponse(
                    ctx,
                    httpRequestWrapper.httpRequest,
                    response,
                    file,
                    listenersProvider
                )
            }

            ByteArray::class.java -> {
                val bytes = returnValueOverride as ByteArray
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                ComposedApiWebHandler.responseNormal(
                    ctx,
                    httpRequestWrapper.httpRequest,
                    newResponse
                )
            }

            DesignResponse::class.java -> {
                val designResponse = returnValueOverride as DesignResponse<*>
                val data = designResponse.data
                val parameterizedType = handlerMethod.method.genericReturnType as ParameterizedType
                val designResponseValueType = parameterizedType.actualTypeArguments[0]
                when (designResponseValueType) {
                    ContentDownloadN::class.java -> {
                        if (data != null) {
                            val contentDownloadN = data as ContentDownloadN
                            handleDownloadContentNResponse(
                                ctx,
                                httpRequestWrapper.httpRequest,
                                response,
                                contentDownloadN,
                                listenersProvider
                            )
                        } else {
                            val bytes = handlerMethod.jsonTransformer.toString(returnValueOverride)
                                .toByteArray()
                            val responseBody = Unpooled.wrappedBuffer(bytes)
                            val newResponse = response.replace(responseBody)
                            ComposedApiWebHandler.responseNormal(
                                ctx,
                                httpRequestWrapper.httpRequest,
                                newResponse
                            )
                        }
                    }

                    File::class.java -> {
                        if (data != null) {
                            val file = designResponse.data as File
                            handleFileContentResponse(
                                ctx,
                                httpRequestWrapper.httpRequest,
                                response,
                                file,
                                listenersProvider
                            )
                        } else {
                            val bytes = handlerMethod.jsonTransformer.toString(returnValueOverride)
                                .toByteArray()
                            val responseBody = Unpooled.wrappedBuffer(bytes)
                            val newResponse = response.replace(responseBody)
                            ComposedApiWebHandler.responseNormal(
                                ctx,
                                httpRequestWrapper.httpRequest,
                                newResponse
                            )
                        }
                    }

                    else -> {
                        val bytes = handlerMethod.jsonTransformer.toString(returnValueOverride)
                            .toByteArray()
                        val responseBody = Unpooled.wrappedBuffer(bytes)
                        val newResponse = response.replace(responseBody)
                        ComposedApiWebHandler.responseNormal(
                            ctx,
                            httpRequestWrapper.httpRequest,
                            newResponse
                        )
                    }
                }

            }

            else -> {
                val bytes = handlerMethod.jsonTransformer.toString(returnValueOverride)
                    .toByteArray()
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                ComposedApiWebHandler.responseNormal(
                    ctx,
                    httpRequestWrapper.httpRequest,
                    newResponse
                )
            }
        }
    }

    private fun handleDownloadContentNResponse(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequest,
        httpResponse: HttpResponse,
        contentDownloadN: ContentDownloadN,
        listenersProvider: ListenersProvider?
    ) {
        val dataContentResponse = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        val headers = dataContentResponse.headers()
        headers.setAll(httpResponse.headers())

        val readableData = contentDownloadN.readableData

        val length = readableData.getLength()

        headers.set(
            HttpHeaderNames.CONTENT_DISPOSITION,
            "attachment; filename=\"" + contentDownloadN.name + "\""
        )
        headers.set(
            HttpHeaderNames.TRANSFER_ENCODING,
            HttpHeaderValues.CHUNKED
        )

        headers.set(HttpHeaderNames.CONTENT_LENGTH, length)

        ctx.write(dataContentResponse)

        when (readableData) {
            is InputStreamReadableData -> {
                val inputStream = readableData.getInputStream()
                val relatedCloseable = readableData.getRelatedCloseable()
                val chunkSize = 8 * 1024
                val chunkedStream = ChunkedStream(inputStream, chunkSize)
                val httpChunkedInput = MyHttpChunkedInput(chunkedStream, length)
                val sendFileChannelFuture =
                    ctx.write(httpChunkedInput, ctx.newProgressivePromise())
                val progressiveFutureListener = ContentDownloadProgressListener(
                    contentDownloadN.id,
                    contentDownloadN.name,
                    length,
                    relatedCloseable,
                    listenersProvider?.getSendProgressListener()
                )
                sendFileChannelFuture.addListener(progressiveFutureListener)
                val lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                if (!HttpUtil.isKeepAlive(httpRequest)) {
                    lastContentFuture.addListener(ChannelFutureListener.CLOSE)
                }
            }
        }
    }

    private fun handleFileContentResponse(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequest,
        httpResponse: HttpResponse,
        file: File,
        listenersProvider: ListenersProvider?
    ) {
        val dataContentResponse = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        val headers = dataContentResponse.headers()
        headers.setAll(httpResponse.headers())

        val length = file.length()

        headers.set(
            HttpHeaderNames.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.name + "\""
        )
        headers.set(
            HttpHeaderNames.TRANSFER_ENCODING,
            HttpHeaderValues.CHUNKED
        )
        headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length())

        ctx.writeAndFlush(dataContentResponse)

        val inputStream = file.inputStream()
        val defaultFileRegion = DefaultFileRegion(inputStream.channel, 0, length)
        val sendFileChannelFuture = ctx.write(defaultFileRegion, ctx.newProgressivePromise())
        val progressiveFutureListener = ContentDownloadProgressListener(
            UUID.randomUUID().toString(),
            file.name,
            length,
            null,
            listenersProvider?.getSendProgressListener()
        )
        sendFileChannelFuture.addListener(progressiveFutureListener)
        val lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
        if (!HttpUtil.isKeepAlive(httpRequest)) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE)
        }
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

    fun getContentType(handlerMethod: HandlerMethod): String? {
        val returnType = handlerMethod.returnType
        when (returnType) {
            Byte::class.java,
            Short::class.java,
            Int::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            String::class.java,
            Enum::class.java,
                -> {
                return ContentType.TEXT_PLAIN
            }

            Annotation::class.java,
            Nothing::class.java,
            Unit::class.java,
            Void::class.java -> {
                return null
            }

            File::class.java,
            ByteArray::class.java,
            ContentDownloadN::class.java -> {
                return ContentType.APPLICATION_OCTET_STREAM
            }

            DesignResponse::class.java -> {
                val parameterizedType = handlerMethod.method.genericReturnType as ParameterizedType
                val designResponseValueType = parameterizedType.actualTypeArguments[0]
                when (designResponseValueType) {
                    ContentDownloadN::class.java -> {
                        return ContentType.APPLICATION_OCTET_STREAM
                    }

                    File::class.java -> {
                        return ContentType.APPLICATION_OCTET_STREAM
                    }

                    else -> {
                        return ContentType.APPLICATION_JSON
                    }
                }
            }

            else -> {
                return ContentType.APPLICATION_JSON
            }
        }
    }
}

class ContentDownloadProgressListener(
    private val id: String,
    private val name: String,
    private val length: Long,
    private val relatedCloseable: Closeable?,
    private val progressListener: ProgressListener?
) : ChannelProgressiveFutureListener {
    companion object {
        private const val TAG = "ContentDownloadProgressListener"
    }

    override fun operationProgressed(
        future: ChannelProgressiveFuture?,
        progress: Long,
        total: Long
    ) {
        if (progressListener != null) {
            val dataProgressInfo =
                DataProgressInfoPool.obtainById(id)
            dataProgressInfo.name = name
            dataProgressInfo.total = if (total != -1L) {
                total
            } else {
                length
            }
            dataProgressInfo.current = progress
            progressListener.onProgress(dataProgressInfo)
        }
    }

    override fun operationComplete(future: ChannelProgressiveFuture) {
        PurpleLogger.current.d(TAG, "operationComplete, id:${id}, name:${name}")
        relatedCloseable?.close()
        future.removeListener(this)
    }
}

class MyHttpChunkedInput(input: ChunkedInput<ByteBuf>, private val dataLength: Long? = null) :
    HttpChunkedInput(input) {
    companion object {
        private const val TAG = "MyHttpChunkedInput"
    }

    override fun readChunk(allocator: ByteBufAllocator?): HttpContent? {
        val readChunk = super.readChunk(allocator)
        return readChunk

    }

    override fun length(): Long {
        val superLength = super.length()
        val returnLength = dataLength ?: superLength
        PurpleLogger.current.d(TAG, "length, superLength:$superLength, returnLength:$returnLength")
        return superLength
    }

    override fun close() {
        val progress = progress()
        PurpleLogger.current.d(TAG, "close, progress:$progress")
        super.close()
    }
}