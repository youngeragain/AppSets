package xcj.app.main

import com.google.gson.Gson
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver
import xcj.app.ApiDesignKeys
import xcj.app.main.util.Helpers
import xcj.app.util.PurpleLogger

@Component
class IPAddressFilter(
    private val designContext: DesignContext,
    private val sharedExceptionHandler: SharedExceptionHandler,
    @Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver
) : Filter {
    companion object {
        private const val TAG = "IPAddressFilter"
        private val ProxyPassedHostnames = mutableListOf("127.0.0.1")
        private var shouldCheckProxy = false
        const val HEADER_PROXY_NGINX = "proxy_nginx"
        const val HEADER_REQUEST_ID = "request_id"
        const val HEADER_RAW_HOST = "raw_host"
        const val HEADER_RAW_PORT = "raw_port"
        const val HEADER_RAW_ADDRESS = "raw_address"
        const val REQUEST_PATH = "request_path"
        const val REQUEST_URI = "request_uri"

    }

    private val gson = Gson()

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (request is HttpServletRequest) {
            if (shouldCheckProxy) {
                val headerNames = request.headerNames.toList()
                if (!headerNames.contains(HEADER_PROXY_NGINX)) {
                    val exception = Exception("Server won't accept a request witch is not from nginx!")
                    resolver.resolveException(
                        request,
                        response as HttpServletResponse,
                        sharedExceptionHandler,
                        exception
                    )

                    return
                }
                val headerOfProxyNginx = request.getHeader(HEADER_PROXY_NGINX)
                if (!ProxyPassedHostnames.contains(headerOfProxyNginx)) {
                    val exception = Exception("Server won't accept a request witch is not valid nginx proxy!")
                    resolver.resolveException(
                        request,
                        response as HttpServletResponse,
                        sharedExceptionHandler,
                        exception
                    )
                    return
                }
            }

            val newRequest = ExtendedRequestWrapper(request)

            /**
             * 对每个请求的请求信息，逻辑日志，代码异常，返回结果都附加到requestId为key,value待定的结构中，便于对其分析处理
             */
            val requestId = Helpers.generateRequestId()
            newRequest.addDesignHeader(HEADER_REQUEST_ID, requestId)
            newRequest.addDesignHeader(REQUEST_URI, request.requestURI)
            newRequest.addDesignHeader(REQUEST_PATH, request.servletPath)
            newRequest.addDesignHeader(HEADER_RAW_HOST, request.remoteHost)
            newRequest.addDesignHeader(HEADER_RAW_PORT, request.remotePort)
            newRequest.addDesignHeader(HEADER_RAW_ADDRESS, request.remoteAddr)
            request.getHeader(ApiDesignKeys.TOKEN_MD5)?.let { tokenMd5 ->
                newRequest.addDesignHeader(ApiDesignKeys.TOKEN_MD5_FIRST_UPPERCASE, tokenMd5)
            }
            request.getHeader(ApiDesignKeys.APP_TOKEN_MD5)?.let { appTokenMd5 ->
                newRequest.addDesignHeader(ApiDesignKeys.APP_TOKEN_MD5_FIRST_UPPERCASE, appTokenMd5)
            }

            doAnalyzeRequest(newRequest)

            chain?.doFilter(newRequest, response)
        } else {
            chain?.doFilter(request, response)
        }
    }

    fun doAnalyzeRequest(request: ExtendedRequestWrapper) {
        designContext.coroutineScope.launch(Dispatchers.IO) {
            val headerMap = mutableMapOf<String, Any?>()
            headerMap.putAll(request.getDesignHeaders())
            for (headerName in request.headerNames) {
                headerMap.put(headerName, request.getHeader(headerName))
            }
            val requestInfoMapJson = gson.toJson(headerMap)
            PurpleLogger.current.d(TAG, "doAnalyzeRequest, headerMap:\n$requestInfoMapJson")
        }
    }
}