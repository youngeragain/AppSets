package xcj.app.web.webserver.netty

import io.netty.buffer.Unpooled
import io.netty.handler.codec.http.*
import java.io.File

interface HandleResult{
    fun getContentType():String?
    fun getResult():Any?
    companion object{
        val EMPTY = object : HandleResult {
            override fun getContentType(): String? = null

            override fun getResult(): Any? = null
        }
    }
}

interface HandlerMapping{
    fun handle(httpRequest: HttpRequest, httpResponse: HttpResponse): HandleResult
    fun isSupport(httpRequest: HttpRequest):Boolean
}


class RequestPathHandlerMapping(
    private val fixedUriHandlerMethods:List<HandlerMethod>?,
    private val dynamicUriHandlerMethods:List<HandlerMethod>?
    ) : HandlerMapping {
    override fun handle(httpRequest: HttpRequest, httpResponse: HttpResponse): HandleResult {
        if(fixedUriHandlerMethods.isNullOrEmpty())
            return HandleResult.EMPTY
        for(handlerMethod in fixedUriHandlerMethods){
            if(WebHandler.queryDecoderLocal.get()?.path()!=handlerMethod.uri)
                continue
            return object : HandleResult {
                override fun getContentType(): String? {
                    return when (handlerMethod.returnType) {
                        String::class.java,
                        Int::class.java,
                        Short::class.java,
                        Long::class.java,
                        Float::class.java,
                        Double::class.java,
                        Enum::class.java,
                        Annotation::class.java -> {
                            "text/plain"
                        }
                        File::class.java,
                        ByteArray::class.java->{
                            "application/octet-stream"
                        }
                        Nothing::class.java,
                        Unit::class.java,
                        Void::class.java -> {
                            null
                        }
                        else -> {
                            "application/json"
                        }
                    }
                }

                override fun getResult(): Any? {
                    val methodInvokeReturnValue = handlerMethod.invoke(httpRequest, httpResponse)
                    val bytes = when (handlerMethod.returnType) {
                        String::class.java,
                        Int::class.java,
                        Short::class.java,
                        Long::class.java,
                        Float::class.java,
                        Double::class.java,
                        Enum::class.java,
                        Annotation::class.java -> {
                            methodInvokeReturnValue.toString().toByteArray()
                        }
                        Nothing::class.java,
                        Unit::class.java,
                        Void::class.java -> {
                            byteArrayOf()
                        }
                        File::class.java->{
                            (methodInvokeReturnValue as File).readBytes()
                        }
                        ByteArray::class.java->{
                            methodInvokeReturnValue as ByteArray
                        }
                        else -> {

                            handlerMethod.jsonWrapper.toJson(methodInvokeReturnValue).toByteArray()
                        }
                    }
                    val responseBody = Unpooled.wrappedBuffer(bytes)
                    val newResponse = (httpResponse as? DefaultFullHttpResponse)?.replace(responseBody)
                    val httpHeaders = newResponse?.headers()
                    //handle Http headers
                    val existResponseContentType = httpHeaders?.get(HttpHeaderNames.CONTENT_TYPE)
                    if(existResponseContentType.isNullOrEmpty()){
                        val guessContentType = getContentType()
                        if(!guessContentType.isNullOrEmpty()){
                            httpHeaders?.set(HttpHeaderNames.CONTENT_TYPE, guessContentType)
                        }
                    }
                    return newResponse
                }
            }
        }
        return HandleResult.EMPTY
    }

    override fun isSupport(httpRequest: HttpRequest): Boolean {
        return true
    }
}