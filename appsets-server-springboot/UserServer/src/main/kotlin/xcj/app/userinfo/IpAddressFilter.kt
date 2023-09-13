package xcj.app.userinfo

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import xcj.app.CoreLogger
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.annotation.Order
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators
import org.springframework.web.servlet.HandlerExceptionResolver
import xcj.app.ApiDesignEncodeStr


@Component
class IpAddressFilter(
    apiDesignContext: ApiDesignContext,
    private val sharedExceptionHandler: SharedExceptionHandler,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver
): Filter {
    companion object{
        val ProxyPassedHostnames = mutableListOf("127.0.0.1")
        var shouldCheckProxy = false
    }
    private val gson = Gson()
    private val coroutineScope = apiDesignContext.coroutineScope
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {

        if(request is HttpServletRequest){
            if(shouldCheckProxy){
                val headerNames = request.headerNames.toList()
                if(!headerNames.contains("proxy_nginx")){
                    val exception = Exception("Server won't accept a request witch is not from nginx!")
                    resolver.resolveException(request, response as HttpServletResponse, sharedExceptionHandler, exception)

                    return
                }
                val headerOfProxyNginx = request.getHeader("proxy_nginx")
                if(!ProxyPassedHostnames.contains(headerOfProxyNginx)){
                    val exception = Exception("Server won't accept a request witch is not valid nginx proxy!")
                    resolver.resolveException(request, response as HttpServletResponse, sharedExceptionHandler, exception)
                    return
                }
            }
            /**
             * 对每个请求的请求信息，逻辑日志，代码异常，返回结果都附加到requestId为key,value待定的结构中，便于对其分析处理
             */

            val newRequest = ExtendedRequestWrapper(request)
            request.getHeader(ApiDesignEncodeStr.tokenStrToMd5)?.let { tokenMd5->
                newRequest.addDesignHeader(ApiDesignEncodeStr.tokenStrToMd5TitleCase, tokenMd5)
            }
            request.getHeader(ApiDesignEncodeStr.appTokenStrToMd5)?.let { appTokenMd5->
                newRequest.addDesignHeader(ApiDesignEncodeStr.appTokenStrToMd5TitleCase, appTokenMd5)
            }
            val requestId = Helpers.generateRequestId()


            doAnalyzeRequest(newRequest, requestId)
            chain?.doFilter(newRequest, response)
        }else{
            chain?.doFilter(request, response)
        }
    }
    fun doAnalyzeRequest(request: ExtendedRequestWrapper, requestId:String){


        request.addDesignHeader("request_id", requestId)
        request.addDesignHeader("r_host", request.remoteHost)
        request.addDesignHeader("r_port", request.remotePort)
        request.addDesignHeader("r_address", request.remoteAddr)
        request.addDesignHeader("r_hpa", "${request.remoteHost}:${request.remotePort}:${request.remoteAddr}")
        coroutineScope.launch(Dispatchers.IO) {
            val map:Map<String, Any?> = mutableMapOf<String, Any?>(
                "requestPath" to request.servletPath
            ).apply {
                putAll(request.getDesignHeaders())
                for (headerName in request.headerNames) {
                    put(headerName, request.getHeader(headerName))
                }
            }
            val requestInfoMapJson = gson.toJson(map)
            CoreLogger.d("IpAddressFilter", "doAnalyzeRequest:\n$requestInfoMapJson")
        }
    }
}