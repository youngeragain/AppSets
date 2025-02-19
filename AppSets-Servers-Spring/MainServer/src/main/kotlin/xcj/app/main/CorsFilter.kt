package xcj.app.main

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import xcj.app.util.PurpleLogger

@Component
class CorsFilter : Filter {

    companion object {
        private const val TAG = "CorsFilter"
    }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            PurpleLogger.current.d(TAG, "doFilter, requestURI:${request.requestURI}")
            //设置访问源地址
            response.setHeader("Access-Control-Allow-Origin", "*")
            //设置访问源请求方法
            response.setHeader("Access-Control-Allow-Methods", "*")
            //跨域请求的有效时间, 这里是1小时
            response.setHeader("Access-Control-Max-Age", "60")
            //设置访问源请求头
            response.setHeader("Access-Control-Allow-Headers", "*")
            response.setHeader("Access-Control-Allow-Credentials", "true")
        }
        chain.doFilter(request, response)
    }
}