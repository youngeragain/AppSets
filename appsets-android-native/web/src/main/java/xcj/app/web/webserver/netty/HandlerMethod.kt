package xcj.app.web.webserver.netty

import android.content.Context
import android.util.Log
import xcj.app.web.webserver.UriSplitResults
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import org.eclipse.jetty.http.HttpMethod
import xcj.app.core.test.Purple
import xcj.app.web.webserver.interfaces.*
import java.lang.reflect.Method

class HandlerMethod{
    //TODO move to val
    lateinit var methodContextObject:Any
    //TODO move to val
    lateinit var method: Method
    //TODO move to val
    lateinit var uri:String
    //TODO move to val
    var uriSplitResults: UriSplitResults? = null
    //TODO move to val
    lateinit var acceptRequestMethods:Array<HttpMethod>
    private val acceptRequestMethodsString by lazy {
        acceptRequestMethods.joinToString { it.asString() }
    }
    //TODO move to val
    var methodArgumentsTypes:Array<Class<*>> = emptyArray()
    //TODO move to val
    var methodArgumentsAnnotations:Array<out Array<out Annotation>> = emptyArray()
    //TODO move to val
    var returnType:Class<*>? = null
    //TODO move to val
    lateinit var jsonWrapper: JavaScriptObjectNotationWrapperInterface


    fun invoke(httpRequest: HttpRequest, httResponse:HttpResponse):Any?{
        kotlin.runCatching {
            val httpMethod = httpRequest.method().name()
            if(acceptRequestMethods.isNotEmpty()&&acceptRequestMethods.firstOrNull { it.asString()==httpMethod }==null){
                val httpMethodNotSupportString =
                    "${toString()} only support http request method is:${acceptRequestMethodsString}, your request method is:${httpRequest.method()}"
                Log.e("blue", httpMethodNotSupportString)
                return httpMethodNotSupportString
            }
            if(methodArgumentsTypes.isEmpty())
                method.invoke(methodContextObject)
            else{
                val methodArgs:Array<Any?> = Array(methodArgumentsTypes.size) { t -> t }
                for((index, argumentType) in methodArgumentsTypes.withIndex()){
                    val argumentAnnotations= methodArgumentsAnnotations[index]
                    val guessedValue = Estimator.guessValue(
                        jsonWrapper,
                        httpRequest,
                        httResponse,
                        argumentType,
                        argumentAnnotations
                    )
                    methodArgs[index] = guessedValue
                }
                method.invoke(methodContextObject, *methodArgs)
            }
        }.onSuccess {
            return it
        }.onFailure {
            Log.e("blue","invoke failed:${it}")
            return null
        }
        return null
    }

    override fun toString(): String {
        if(uriSplitResults!=null){
            return "path:${uri} <> ${uriSplitResults}\n"
        }
        return "path:$uri\n"
    }
}
object Estimator{
    //TODO 判断多个注解到入参变量可能冲突的问题
    fun guessValue(
        jsonWrapper: JavaScriptObjectNotationWrapperInterface,
        httpRequest: HttpRequest,
        httpResponse: HttpResponse,
        type: Class<*>,
        argumentAnnotations: Array<out Annotation>
    ):Any? {
        if(type==HttpRequest::class.java)
            return httpRequest
        if(type==HttpResponse::class.java)
            return httpResponse
        if(type==Context::class.java){
            val annotationOfHttpHeader:Annotation? = argumentAnnotations.firstOrNull { it.annotationClass== AndroidContext::class }
            if(annotationOfHttpHeader!=null){
                val androidContext = annotationOfHttpHeader as AndroidContext
                val purple = Purple.getPurpleContext() ?: return null
                if(androidContext.type==0){
                    return purple.androidContexts?.application
                }
                if(androidContext.type==1)
                    return purple.androidContexts?.getTopActivityContext()
                if(androidContext.type==2){
                    //TODO
                    return null
                }
            }
        }
        if(argumentAnnotations.isNotEmpty()){
            // TODO find in early
            var annotationOfHttpHeader:Annotation? = null
            var annotationOfHttpBody:Annotation? = null
            var annotationOfHttpQueryParam:Annotation? = null
            var annotationOfHttpPathVariable:Annotation? = null
            var annotationOfHttpQueryParamOrder:Annotation? = null
            for(argumentAnnotation in argumentAnnotations){
                when(argumentAnnotation.annotationClass.java){
                    HttpHeader::class.java->
                        annotationOfHttpHeader = argumentAnnotation
                    HttpBody::class.java->
                        annotationOfHttpBody = argumentAnnotation
                    HttpQueryParam::class.java->
                        annotationOfHttpQueryParam = argumentAnnotation
                    HttpPathVariable::class.java->
                        annotationOfHttpPathVariable = argumentAnnotation
                    HttpQueryParamOrder::class.java->
                        annotationOfHttpQueryParamOrder = argumentAnnotation
                }
            }

            if(annotationOfHttpHeader !=null){
                val header = httpRequest.headers().get((annotationOfHttpHeader as HttpHeader).name)
                return resolveValueType(type, header)
            }

            else if(annotationOfHttpBody != null){
                val contentType = httpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE).lowercase()
                return if(contentType.startsWith("application/json")){
                    //raw json
                    resolveRawJsonOrTextBody(httpRequest, jsonWrapper, type)
                }else if(contentType.startsWith("text/plain")){
                    return resolveRawJsonOrTextBody(httpRequest, null, null)
                }else{
                    //"form-data or file"
                    resolveFormDataOrFileBody(jsonWrapper, httpRequest, type)
                    null
                }
            }

            else if(annotationOfHttpQueryParam != null){
                //注意最大查询pair限制
                //http://localhost/querysample?name=雷军&age=12&name=小巧&sex=female
                val queryStringDecoder = WebHandler.queryDecoderLocal.get()?: QueryStringDecoder(httpRequest.uri())
                val queryParamsWithName = queryStringDecoder.parameters()[(annotationOfHttpQueryParam as HttpQueryParam).name]
                if(queryParamsWithName.isNullOrEmpty())
                    return null
                if(annotationOfHttpQueryParamOrder == null)
                    return queryParamsWithName.firstOrNull()
                val order = (annotationOfHttpQueryParamOrder as HttpQueryParamOrder).order
                return if(order>=queryParamsWithName.size || order<0){
                    queryParamsWithName.firstOrNull()
                }else{
                    queryParamsWithName.getOrNull(order)
                }
            }

            else if(annotationOfHttpPathVariable != null){

            }
        }

        return null
    }

    private fun resolveValueType(type: Class<*>, rawValue:Any?):Any?{
        try {
            if(type==String::class.java)
                return rawValue.toString()
            if(type==Int::class.java||type==java.lang.Integer::class.java)
                return rawValue.toString().toInt()
            if(type==Float::class.java)
                return rawValue.toString().toFloat()
            if(type==Double::class.java)
                return rawValue.toString().toDouble()
            if(type==Short::class.java)
                return rawValue.toString().toShort()
            if(type==Long::class.java)
                return rawValue.toString().toLong()
        }catch (e:Exception){
            e.printStackTrace()
            return null
        }
        return null
    }

    private fun resolveFormDataOrFileBody(gson: JavaScriptObjectNotationWrapperInterface, httpRequest: HttpRequest, type: Class<*>):Any? {
        //1.clearly form data
        //2.file data
        val content = (httpRequest as FullHttpMessage).content()
        if(!content.isReadable)
            return null
        //HttpPostRequestDecoder针对post请求提交表单数据以及文件上传时使用
        val httpPostRequestDecoder: HttpPostRequestDecoder = HttpPostRequestDecoder(httpRequest)

        httpPostRequestDecoder.bodyHttpDatas.forEach {
            Log.e("blue", "表单数据或文件:$it")
        }
        httpPostRequestDecoder.destroy()
        return null
    }

    private fun resolveRawJsonOrTextBody(httpRequest: HttpRequest, gson: JavaScriptObjectNotationWrapperInterface?, type: Class<*>?): Any? {
        try {
            val content = (httpRequest as FullHttpMessage).content()
            if(!content.isReadable)
                return null
            val readableBytes = content.readableBytes()
            if(readableBytes == 0)
                return null
            val bodyArray = content.alloc().buffer(content.readableBytes()).writeBytes(content).array()
            val httpBody = String(bodyArray)
            if(gson!=null&&type!=null)
                return gson.fromJson(httpBody, type)
            return httpBody
        }catch (e:Exception){
            e.printStackTrace()
            return null
        }
    }
}