package xcj.app.web.webserver.netty

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponse
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import java.io.File
import kotlin.text.isNullOrEmpty

class DefaultHandleResult(
    private val handlerMethod: HandlerMethod,
    private val ctx: ChannelHandlerContext,
    private val httpRequest: HttpRequestWrapper,
    private val httpResponse: HttpResponseWrapper
) : HandleResult {

    companion object {
        private const val TAG = "DefaultHandleResult"
    }

    override fun getContentType(): String? {
        when (handlerMethod.returnType) {
            String::class.java,
            Int::class.java,
            Short::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            Enum::class.java,
            Annotation::class.java -> {
                return ContentType.TEXT_PLAIN
            }

            File::class.java,
            ByteArray::class.java -> {
                return ContentType.APPLICATION_OCTET_STREAM
            }

            Nothing::class.java,
            Unit::class.java,
            Void::class.java -> {
                return null
            }

            else -> {
                return ContentType.APPLICATION_JSON
            }
        }
    }

    override fun getResult(): HttpResponse? {
        val returnValue = handlerMethod.call(ctx, httpRequest, httpResponse)
        val bytes = when (handlerMethod.returnType) {
            String::class.java,
            Int::class.java,
            Short::class.java,
            Long::class.java,
            Float::class.java,
            Double::class.java,
            Enum::class.java,
            Annotation::class.java -> {
                returnValue.toString().toByteArray()
            }

            Nothing::class.java,
            Unit::class.java,
            Void::class.java -> {
                byteArrayOf()
            }

            File::class.java -> {
                (returnValue as File).readBytes()
            }

            ByteArray::class.java -> {
                returnValue as ByteArray
            }

            else -> {
                handlerMethod.jsonTransformer.toString(returnValue)
                    .toByteArray()
            }
        }
        val responseBody = Unpooled.wrappedBuffer(bytes)
        val response = httpResponse.httpResponse as? DefaultFullHttpResponse
        val newResponse = response?.replace(responseBody)

        val guessContentType = getContentType()
        if (!guessContentType.isNullOrEmpty()) {
            val httpHeaders = newResponse?.headers()
            //handle Http headers
            httpHeaders?.set(HttpHeaderNames.CONTENT_TYPE, guessContentType)
        }
        return newResponse
    }
}