package xcj.app.web.webserver.netty

import android.content.Context
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.test.LocalTopActivity
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.FileUploadN
import xcj.app.web.webserver.interfaces.*
import java.io.File
import java.net.InetSocketAddress

object Estimator {

    private const val TAG = "Estimator"

    private val sDefaultHttpDataFactory: ThreadLocal<DefaultHttpDataFactory> = ThreadLocal()

    //TODO 判断多个注解到入参变量可能冲突的问题
    @JvmStatic
    fun guessValue(
        handlerMethod: HandlerMethod,
        ctx: ChannelHandlerContext,
        context: Context,
        contentTransformer: ContentTransformer,
        httpRequest: HttpRequestWrapper,
        httpResponse: HttpResponseWrapper,
        paramType: Class<*>,
        paramIndex: Int,
        argumentAnnotations: Array<out Annotation>
    ): Any? {
        PurpleLogger.current.d(
            TAG,
            "guessValue[${handlerMethod.method.name}], Api Uri:${handlerMethod.uri}, paramIndex:$paramIndex, paramType:$paramType, argumentAnnotations count:${argumentAnnotations.size}"
        )
        if (paramType == HttpRequest::class.java) {
            return httpRequest.httpRequest
        }
        if (paramType == HttpRequestWrapper::class.java) {
            return httpRequest
        }
        if (paramType == HttpResponse::class.java) {
            return httpResponse.httpResponse
        }
        if (paramType == HttpResponseWrapper::class.java) {
            paramType == httpResponse
        }
        if (paramType == Context::class.java) {
            val annotationOfHttpHeader: Annotation? =
                argumentAnnotations.firstOrNull { it.annotationClass == AndroidContext::class }
            if (annotationOfHttpHeader != null) {
                val androidContext = annotationOfHttpHeader as AndroidContext
                if (androidContext.type == AndroidContext.TYPE_APPLICATION) {
                    return context.applicationContext
                }
                if (androidContext.type == AndroidContext.TYPE_ACTIVITY) {
                    return LocalTopActivity
                }
                if (androidContext.type == AndroidContext.TYPE_SERVICE) {
                    //TODO
                    return null
                }
                if (androidContext.type == AndroidContext.TYPE_PROVIDER) {
                    //TODO
                    return null
                }
            }
        }
        //TODO
        if (argumentAnnotations.isNotEmpty()) {
            for (argumentAnnotation in argumentAnnotations) {
                when (argumentAnnotation.annotationClass.java) {
                    RequestInfo::class.java -> {
                        val what = (argumentAnnotation as RequestInfo).what
                        if (what == RequestInfo.WHAT_REQUEST_HOST) {
                            val host = httpRequest.httpRequest.headers().get(HttpHeaderNames.HOST)
                            return resolveSimpleValueForType(paramType, host)
                        }
                        if (what == RequestInfo.WHAT_REQUEST_REMOTE_HOST) {
                            val address = ctx.channel().remoteAddress() as? InetSocketAddress
                            if (address != null) {
                                val remoteIp = address.address.hostAddress
                                val remotePort = address.port
                                val remoteHost = "$remoteIp:$remotePort"
                                return resolveSimpleValueForType(paramType, remoteHost)
                            }
                        }
                    }

                    HttpHeader::class.java -> {
                        val header =
                            httpRequest.httpRequest.headers()
                                .get((argumentAnnotation as HttpHeader).name)
                        return resolveSimpleValueForType(paramType, header)
                    }

                    HttpBody::class.java, RequestBody::class.java -> {
                        val contentType =
                            httpRequest.httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE)
                                .lowercase()
                        when (contentType) {
                            ContentType.APPLICATION_JSON -> {
                                return resolveRawJsonBody(
                                    httpRequest.httpRequest,
                                    contentTransformer,
                                    paramType
                                )
                            }

                            ContentType.TEXT_PLAIN -> {
                                val resolveRawTextBody = resolveRawTextBody(httpRequest.httpRequest)
                                return resolveSimpleValueForType(paramType, resolveRawTextBody)
                            }

                            ContentType.APPLICATION_FORM_DATA -> {
                                return resolveFormDataBody(
                                    contentTransformer,
                                    httpRequest.httpRequest,
                                    paramType
                                )
                            }
                        }

                        if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA)) {
                            return resolveFormDataFileBody(
                                contentTransformer,
                                httpRequest.httpRequest,
                                paramType
                            )
                        }
                        val resolveRawTextBody = resolveRawTextBody(httpRequest.httpRequest)
                        return resolveSimpleValueForType(paramType, resolveRawTextBody)

                    }

                    HttpQueryParam::class.java -> {
                        //注意最大查询pair限制
                        //http://localhost/querysample?name=tom&age=12&name=jerry&sex=female
                        val queryStringDecoder =
                            httpRequest.queryStringDecoder
                        val queryParamsWithName =
                            queryStringDecoder.parameters()[(argumentAnnotation as HttpQueryParam).name]
                        if (queryParamsWithName.isNullOrEmpty()) {
                            return null
                        }
                        var annotationOfHttpQueryParamOrder: Annotation? =
                            argumentAnnotations.firstOrNull {
                                it.annotationClass.java == HttpQueryParamOrder::class.java
                            }
                        if (annotationOfHttpQueryParamOrder == null) {
                            return queryParamsWithName.firstOrNull()
                        }
                        val order = (annotationOfHttpQueryParamOrder as HttpQueryParamOrder).order
                        if (order >= queryParamsWithName.size || order < 0) {
                            return queryParamsWithName.firstOrNull()
                        } else {
                            return queryParamsWithName.getOrNull(order)
                        }
                    }

                    HttpPathVariable::class.java -> {
                        return null
                    }

                    HttpQueryParamOrder::class.java -> {
                        return null
                    }
                }
            }
        }

        return null
    }

    private fun resolveFormDataBody(
        gson: ContentTransformer,
        httpRequest: HttpRequest,
        type: Class<*>
    ): Any? {
        PurpleLogger.current.d(TAG, "resolveFormDataBody")
        //clearly form data
        val content = (httpRequest as FullHttpMessage).content()
        if (!content.isReadable) {
            return null
        }
        //HttpPostRequestDecoder针对post请求提交表单数据以及文件上传时使用
        val httpPostRequestDecoder: HttpPostRequestDecoder = HttpPostRequestDecoder(httpRequest)
        httpPostRequestDecoder.bodyHttpDatas.forEach {
            PurpleLogger.current.d(TAG, "resolveFormDataBody:$it")
        }
        httpPostRequestDecoder.destroy()
        return null
    }

    private fun resolveFormDataFileBody(
        contentTransformer: ContentTransformer,
        httpRequest: HttpRequest,
        type: Class<*>
    ): FileUploadN? {
        PurpleLogger.current.d(TAG, "resolveFormDataFileBody")
        var firstUploadN: FileUploadN? = null
        var fileUploadN: FileUploadN? = null
        var fileUploadNCount = 0

        val defaultHttpDataFactory = DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE)
        val shareDirPath = getShareDirPath()
        defaultHttpDataFactory.setBaseDir(shareDirPath)
        val httpPostRequestDecoder = HttpPostRequestDecoder(
            defaultHttpDataFactory,
            httpRequest
        )

        while (httpPostRequestDecoder.hasNext()) {
            val interfaceHttpData = httpPostRequestDecoder.next()
            if (interfaceHttpData.httpDataType != InterfaceHttpData.HttpDataType.FileUpload) {
                continue
            }
            if (interfaceHttpData !is FileUpload) {
                continue
            }
            fileUploadNCount += 1
            val currentFileUploadN = FileUploadN(interfaceHttpData)
            if (fileUploadN == null) {
                firstUploadN = currentFileUploadN
            }
            fileUploadN?.next = currentFileUploadN
            fileUploadN = currentFileUploadN
        }
        firstUploadN?.amount = fileUploadNCount
        firstUploadN?.httpPostRequestDecoder = httpPostRequestDecoder
        return firstUploadN
    }

    fun getShareDirPath(): String {
        val path = LocalAndroidContextFileDir.current.appSetsShareDir + File.separator
        return path
    }

    private fun resolveRawJsonBody(
        httpRequest: HttpRequest,
        contentTransformer: ContentTransformer,
        type: Class<*>
    ): Any? {
        PurpleLogger.current.d(TAG, "resolveRawJsonBody")
        val fullHttpMessage = httpRequest as? FullHttpMessage
        if (fullHttpMessage == null) {
            return null
        }
        val content = fullHttpMessage.content()
        val readableBytes = content.readableBytes()
        if (readableBytes == 0) {
            return null
        }
        try {
            val bodyArray =
                content.alloc().buffer(content.readableBytes()).writeBytes(content).array()
            val httpBody = String(bodyArray)
            return contentTransformer.fromString(httpBody, type)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun resolveRawTextBody(
        httpRequest: HttpRequest
    ): Any? {
        PurpleLogger.current.d(TAG, "resolveRawTextBody")
        val fullHttpMessage = httpRequest as? FullHttpMessage
        if (fullHttpMessage == null) {
            return null
        }
        val content = fullHttpMessage.content()

        if (!content.isReadable) {
            return null
        }
        val readableBytes = content.readableBytes()
        if (readableBytes == 0) {
            return null
        }
        try {
            val bodyArray =
                content.alloc().buffer(content.readableBytes()).writeBytes(content).array()
            val httpBody = String(bodyArray).trimStart('"').trimEnd('"')
            return httpBody
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun resolveSimpleValueForType(type: Class<*>, rawValue: Any?): Any? {
        if (rawValue == null) {
            return null
        }
        try {
            if (type == String::class.java) {
                return rawValue.toString()
            }
            if (type.name == "int" || type == Int::class.java || type == java.lang.Integer::class.java) {
                return rawValue.toString().toIntOrNull()
            }
            if (type.name == "float" || type == Float::class.java) {
                return rawValue.toString().toFloatOrNull()
            }
            if (type.name == "double" || type == Double::class.java) {
                return rawValue.toString().toDoubleOrNull()
            }
            if (type.name == "short" || type == Short::class.java) {
                return rawValue.toString().toShortOrNull()
            }
            if (type.name == "long" || type == Long::class.java) {
                return rawValue.toString().toLongOrNull()
            }
            if (type.name == "bool" || type.name == "boolean" || type == Boolean::class.java) {
                return rawValue.toString().toBooleanStrictOrNull()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return null
    }
}