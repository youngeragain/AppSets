package xcj.app.userinfo

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.annotation.WebFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import xcj.app.CoreLogger

@Order(2)
@Component
class CorsFilter: Filter {
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        CoreLogger.d("CorsFilter", "doFilter")
        val responseTemp = response as HttpServletResponse
        //设置访问源地址
        responseTemp.setHeader("Access-Control-Allow-Origin", "*")
        //设置访问源请求方法
        responseTemp.setHeader("Access-Control-Allow-Methods", "*")
        //跨域请求的有效时间, 这里是1小时
        responseTemp.setHeader("Access-Control-Max-Age", "3600")
        //设置访问源请求头
        responseTemp.setHeader("Access-Control-Allow-Headers", "*")
        responseTemp.setHeader("Access-Control-Allow-Credentials", "true")
        chain.doFilter(request, response)
    }
}