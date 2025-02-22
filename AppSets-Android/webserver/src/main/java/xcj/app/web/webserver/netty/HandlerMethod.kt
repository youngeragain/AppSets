package xcj.app.web.webserver.netty

import android.content.Context
import io.netty.channel.ChannelHandlerContext
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.UriSplitResults
import xcj.app.web.webserver.interfaces.HttpMethod
import java.lang.AutoCloseable
import java.lang.reflect.Method

class HandlerMethod(
    val context: Context,
    val methodContextObject: Any,
    val method: Method,
    val uri: String,
    val httpMethods: Array<HttpMethod>,
    val jsonTransformer: ContentTransformer
) {
    companion object {
        private const val TAG = "HandlerMethod"
    }

    val uriSplitResults: UriSplitResults? = null

    val methodArgumentTypes: Array<Class<*>>
        get() = method.parameterTypes ?: emptyArray()

    val methodArgumentAnnotations: Array<Array<Annotation>>
        get() = method.parameterAnnotations

    val returnType: Class<*>?
        get() = method.returnType

    val acceptRequestMethodsString: String
        get() = httpMethods.joinToString { it.readableName().uppercase() }

    fun call(
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httpResponseWrapper: HttpResponseWrapper
    ): Any? {
        val acceptRequestMethods = httpMethods
        if (acceptRequestMethods.isEmpty()) {
            return null
        }

        val httpMethod = httpRequestWrapper.httpRequest.method().name()
        val firstCanAcceptMethod = acceptRequestMethods.firstOrNull {
            it.readableName().uppercase() == httpMethod.uppercase()
        }
        if (firstCanAcceptMethod == null) {
            val httpMethodNotSupportString =
                ("method: ${toString()}, info: only support http request method" +
                        " is:${acceptRequestMethodsString}, " +
                        "your request method is:${httpRequestWrapper.httpRequest.method()}")
            PurpleLogger.current.d(TAG, httpMethodNotSupportString)
            val exception =
                DefaultExceptionProvider.provideException(httpMethodNotSupportString)
            return exception
        }
        if (methodArgumentTypes.isEmpty()) {
            try {
                return method.invoke(methodContextObject)
            } catch (e: Exception) {
                e.printStackTrace()
                PurpleLogger.current.d(TAG, "invoke failed:${e.message}")
                val exception =
                    DefaultExceptionProvider.provideException("Server Error")
                return exception
            }
        } else {
            var methodArgs: Array<Any?>? = null
            try {
                methodArgs = buildMethodArgs(this, ctx, httpRequestWrapper, httpResponseWrapper)
                val methodReturnValue = method.invoke(methodContextObject, *methodArgs)
                return methodReturnValue
            } catch (e: Exception) {
                e.printStackTrace()
                PurpleLogger.current.d(TAG, "invoke failed:${e.message}")
                val exception =
                    DefaultExceptionProvider.provideException("Server Error")
                return exception
            } finally {
                releaseArgsIfNeeded(methodArgs)
            }
        }
        return null
    }

    private fun releaseArgsIfNeeded(values: Array<Any?>?) {
        PurpleLogger.current.d(TAG, "releaseArgsIfNeeded")
        if (values.isNullOrEmpty()) {
            return
        }
        values.forEach { value ->
            if (value is AutoCloseable) {
                value.close()
            }
        }
    }

    private fun buildMethodArgs(
        handlerMethod: HandlerMethod,
        ctx: ChannelHandlerContext,
        httpRequestWrapper: HttpRequestWrapper,
        httResponseWrapper: HttpResponseWrapper
    ): Array<Any?> {
        PurpleLogger.current.d(
            TAG,
            "buildMethodArgs[${handlerMethod.method.name}], Api Uri:${handlerMethod.uri}"
        )
        val methodArgs: Array<Any?> = Array(methodArgumentTypes.size) { t -> t }
        for ((index, argumentType) in methodArgumentTypes.withIndex()) {
            val argumentAnnotations = methodArgumentAnnotations[index]
            val guessedValue = ParamsValueHandler.guessValue(
                handlerMethod,
                ctx,
                context,
                jsonTransformer,
                httpRequestWrapper,
                httResponseWrapper,
                argumentType,
                index,
                argumentAnnotations
            )
            methodArgs[index] = guessedValue
        }
        PurpleLogger.current.d(
            TAG,
            "buildMethodArgs[${handlerMethod.method.name}], Api Uri:${handlerMethod.uri}, " +
                    "methodArgs:${methodArgs.joinToString()}"
        )
        return methodArgs
    }

    override fun toString(): String {
        return uri
    }
}

