package xcj.app.web.webserver.netty

import android.content.Context
import io.netty.channel.ChannelHandlerContext
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.FileUploadN
import xcj.app.web.webserver.base.UriSplitResults
import xcj.app.web.webserver.interfaces.HttpMethod
import java.io.Closeable
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

    fun isHanlePostFileUri(): Boolean {
        return uri == "/appsets/share/file"
    }

    fun call(
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequestWrapper,
        httResponse: HttpResponseWrapper
    ): Any? {
        val acceptRequestMethods = httpMethods
        if (acceptRequestMethods.isEmpty()) {
            return null
        }

        val httpMethod = httpRequest.httpRequest.method().name()
        val methodCanAccept = acceptRequestMethods.firstOrNull {
            it.readableName().uppercase() == httpMethod.uppercase()
        } != null
        if (!methodCanAccept) {
            val httpMethodNotSupportString =
                ("method: ${toString()}, info: only support http request method" +
                        " is:${acceptRequestMethodsString}, " +
                        "your request method is:${httpRequest.httpRequest.method()}")
            PurpleLogger.current.d(TAG, httpMethodNotSupportString)
            val exception =
                DefaultExceptionProvider.provideException(httpMethodNotSupportString)
            return exception
        }
        runCatching {
            if (methodArgumentTypes.isEmpty()) {
                return method.invoke(methodContextObject)
            } else {
                val methodArgs = buildMethodArgs(this, ctx, httpRequest, httResponse)
                val methodReturnValue = method.invoke(methodContextObject, *methodArgs)
                releaseIfNeeded(methodArgs)
                return methodReturnValue
            }
        }.onFailure {
            it.printStackTrace()
            PurpleLogger.current.d(TAG, "invoke failed:${it}")
            val exception =
                DefaultExceptionProvider.provideException("Server Error")
            return exception
        }
        return null
    }

    private fun releaseIfNeeded(values: Array<Any?>) {
        PurpleLogger.current.d(TAG, "releaseIfNeeded")
        var hasFileUploadN = false
        values.forEach { value ->
            if (value is Closeable) {
                value.close()
            } else if (value is AutoCloseable) {
                value.close()
            }
            if (value is FileUploadN) {
                hasFileUploadN = true
            }
        }
        if (hasFileUploadN) {
            PurpleLogger.current.d(
                TAG,
                "releaseIfNeeded, found a fileFileUploadN, make System GC once if needed!"
            )
            System.gc()
        }
    }

    private fun buildMethodArgs(
        handlerMethod: HandlerMethod,
        ctx: ChannelHandlerContext,
        httpRequest: HttpRequestWrapper,
        httResponse: HttpResponseWrapper
    ): Array<Any?> {
        PurpleLogger.current.d(
            TAG,
            "buildMethodArgs[${handlerMethod.method.name}], Api Uri:${handlerMethod.uri}"
        )
        val methodArgs: Array<Any?> = Array(methodArgumentTypes.size) { t -> t }
        for ((index, argumentType) in methodArgumentTypes.withIndex()) {
            val argumentAnnotations = methodArgumentAnnotations[index]
            val guessedValue = Estimator.guessValue(
                handlerMethod,
                ctx,
                context,
                jsonTransformer,
                httpRequest,
                httResponse,
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

