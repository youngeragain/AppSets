package xcj.app.web.webserver.netty

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelProgressiveFuture
import io.netty.channel.ChannelProgressiveFutureListener
import io.netty.channel.DefaultFileRegion
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponse
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.codec.http.LastHttpContent
import io.netty.handler.stream.ChunkedStream
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.ContentDownloadN
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.base.FileChanelReadableData
import xcj.app.web.webserver.base.InputStreamReadableData
import java.io.File
import java.lang.reflect.ParameterizedType

class RequestPathHandlerMapping(
    val fixedUriHandlerMethodMap: Map<String, HandlerMethod>,
    val dynamicUriHandlerMethodMap: Map<String, HandlerMethod>
) : HandlerMapping {
    companion object {
        private const val TAG = "RequestPathHandlerMapping"
        private val EMPTY_BYTES = byteArrayOf()
    }

    override fun handle(
        webHanlder: WebHandler,
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper
    ) {

        val handlerMethod = getHandlerMethod(httpRequestWrapper)
        if (handlerMethod == null) {
            PurpleLogger.current.w(
                TAG,
                "handle, handlerMethod is null!"
            )
            webHanlder.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                webHanlder.notFoundUriResponse
            )
            return
        }

        handleInternal(webHanlder, ctx, httpRequestWrapper, httpResponseWrapper, handlerMethod)
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

    private fun handleInternal(
        webHandler: WebHandler,
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper,
        handlerMethod: HandlerMethod
    ) {
        val response = httpResponseWrapper.httpResponse as? DefaultFullHttpResponse
        if (response == null) {
            webHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                webHandler.serverInternalErrorResponse
            )
            return
        }
        val httpHeaders = response.headers()

        if (HttpUtil.isKeepAlive(httpRequestWrapper.httpRequest)) {
            httpHeaders.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
        }
        val guessContentType = getContentType(handlerMethod)
        if (!guessContentType.isNullOrEmpty()) {
            httpHeaders.set(HttpHeaderNames.CONTENT_TYPE, guessContentType)
        }

        val returnValue = handlerMethod.call(ctx, httpRequestWrapper, httpResponseWrapper)

        if (returnValue is Exception) {
            PurpleLogger.current.d(
                TAG,
                "handleInternal, returnValue is Exception:${returnValue.message}"
            )
            webHandler.responseNormal(
                ctx,
                httpRequestWrapper.httpRequest,
                webHandler.serverInternalErrorResponse
            )
            return
        }

        if (returnValue == null) {
            val bytes = EMPTY_BYTES
            httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, bytes.size)
            val responseBody = Unpooled.wrappedBuffer(bytes)
            val newResponse = response.replace(responseBody)
            webHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
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
                val bytes = returnValue.toString().toByteArray()
                httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, bytes.size)
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                webHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
            }

            Annotation::class.java,
            Nothing::class.java,
            Unit::class.java,
            Void::class.java -> {
                val bytes = EMPTY_BYTES
                httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH, bytes.size)
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                webHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
            }

            File::class.java -> {
                val file = returnValue as File
                handleFileContentResponse(
                    webHandler,
                    ctx,
                    httpRequestWrapper.httpRequest,
                    response,
                    file
                )
            }

            ByteArray::class.java -> {
                val bytes = returnValue as ByteArray
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                webHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
            }

            DesignResponse::class.java -> {
                val designResponse = returnValue as DesignResponse<*>
                val parameterizedType = handlerMethod.method.genericReturnType as ParameterizedType
                val designResponseValueType = parameterizedType.actualTypeArguments[0]
                when (designResponseValueType) {
                    ContentDownloadN::class.java -> {
                        val contentDownloadN = designResponse.data as ContentDownloadN
                        handleDownloadContentNResponse(
                            webHandler,
                            ctx,
                            httpRequestWrapper.httpRequest,
                            response,
                            contentDownloadN
                        )
                    }

                    File::class.java -> {
                        val file = designResponse.data as File
                        handleFileContentResponse(
                            webHandler,
                            ctx,
                            httpRequestWrapper.httpRequest,
                            response,
                            file
                        )
                    }

                    else -> {
                        val bytes = handlerMethod.jsonTransformer.toString(returnValue)
                            .toByteArray()
                        val responseBody = Unpooled.wrappedBuffer(bytes)
                        val newResponse = response.replace(responseBody)
                        webHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
                    }
                }

            }

            else -> {
                val bytes = handlerMethod.jsonTransformer.toString(returnValue)
                    .toByteArray()
                val responseBody = Unpooled.wrappedBuffer(bytes)
                val newResponse = response.replace(responseBody)
                webHandler.responseNormal(ctx, httpRequestWrapper.httpRequest, newResponse)
            }
        }
    }

    private fun handleDownloadContentNResponse(
        webHandler: WebHandler,
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequest,
        httpResponse: HttpResponse,
        contentDownloadN: ContentDownloadN
    ) {
        val dataContentResponse = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        val headers = dataContentResponse.headers()
        headers.setAll(httpResponse.headers())

        val readableData = contentDownloadN.readableData
        val chunkSize = 8 * 1024
        val length = readableData.getLength()
        headers.set(HttpHeaderNames.CONTENT_LENGTH, length)
        headers.set(
            HttpHeaderNames.CONTENT_DISPOSITION,
            "attachment; filename=\"" + contentDownloadN.name + "\""
        )

        ctx.write(dataContentResponse)

        when (readableData) {
            is InputStreamReadableData -> {
                val chunkedStream = ChunkedStream(readableData.getInputStream(), chunkSize)

                var channelFuture = ctx.write(chunkedStream, ctx.newProgressivePromise())
                channelFuture.addListener(object : ChannelProgressiveFutureListener {
                    override fun operationProgressed(
                        future: ChannelProgressiveFuture?,
                        progress: Long,
                        total: Long
                    ) {
                        val progressListener = contentDownloadN.progressListener
                        if (progressListener != null) {
                            val dataProgressInfo =
                                DataProgressInfoPool.obtainById(contentDownloadN.id)
                            dataProgressInfo.name = contentDownloadN.name
                            dataProgressInfo.total = length
                            dataProgressInfo.current = progress
                            progressListener.onProgress(dataProgressInfo)
                        }

                    }

                    override fun operationComplete(future: ChannelProgressiveFuture?) {
                        PurpleLogger.current.d(
                            TAG,
                            "operationComplete, contentDownloadN: id:${contentDownloadN.id}, " +
                                    "name:${contentDownloadN.name}"
                        )
                    }
                })
                channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                if (!HttpUtil.isKeepAlive(httpRequest)) {
                    channelFuture.addListener { future -> ctx.disconnect() }
                } else {
                    channelFuture.addListener(ChannelFutureListener.CLOSE)
                }
            }

            is FileChanelReadableData -> {

                val chunkedStream = DefaultFileRegion(readableData.getFileChannel(), 0, length)

                var channelFuture = ctx.write(chunkedStream, ctx.newProgressivePromise())
                channelFuture.addListener(object : ChannelProgressiveFutureListener {
                    override fun operationProgressed(
                        future: ChannelProgressiveFuture?,
                        progress: Long,
                        total: Long
                    ) {
                        val progressListener = contentDownloadN.progressListener
                        if (progressListener != null) {
                            val dataProgressInfo =
                                DataProgressInfoPool.obtainById(contentDownloadN.id)
                            dataProgressInfo.name = contentDownloadN.name
                            dataProgressInfo.total = length
                            dataProgressInfo.current = progress
                            progressListener.onProgress(dataProgressInfo)
                        }
                    }

                    override fun operationComplete(future: ChannelProgressiveFuture?) {
                        PurpleLogger.current.d(
                            TAG,
                            "operationComplete, contentDownloadN: id:${contentDownloadN.id}, " +
                                    "name:${contentDownloadN.name}"
                        )
                    }
                })
                channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
                if (!HttpUtil.isKeepAlive(httpRequest)) {
                    channelFuture.addListener { future -> ctx.disconnect() }
                } else {
                    channelFuture.addListener(ChannelFutureListener.CLOSE)
                }
            }
        }
    }

    private fun handleFileContentResponse(
        webHandler: WebHandler,
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequest,
        httpResponse: HttpResponse,
        file: File
    ) {
        val dataContentResponse = DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)
        val headers = dataContentResponse.headers()
        headers.setAll(httpResponse.headers())

        val chunkSize = 8 * 1024
        val length = file.length()
        headers.set(HttpHeaderNames.CONTENT_LENGTH, file.length())
        headers.set(
            HttpHeaderNames.CONTENT_DISPOSITION,
            "attachment; filename=\"" + file.name + "\""
        )
        ctx.write(dataContentResponse)

        val defaultFileRegion = DefaultFileRegion(file.inputStream().channel, 0, length)
        var channelFuture = ctx.write(defaultFileRegion, ctx.newProgressivePromise())
        channelFuture.addListener(object : ChannelProgressiveFutureListener {
            override fun operationProgressed(
                future: ChannelProgressiveFuture?,
                progress: Long,
                total: Long
            ) {
                PurpleLogger.current.d(
                    TAG,
                    "operationProgressed: file:$file, total:${total} progress:$progress"
                )
            }

            override fun operationComplete(future: ChannelProgressiveFuture?) {
                PurpleLogger.current.d(
                    TAG,
                    "operationProgressed: file:$file"
                )
            }
        })
        channelFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
        if (!HttpUtil.isKeepAlive(httpRequest)) {
            channelFuture.addListener { future -> ctx.disconnect() }
        } else {
            channelFuture.addListener(ChannelFutureListener.CLOSE)
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
}