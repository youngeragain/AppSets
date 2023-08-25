package xcj.app.userinfo.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import reactor.core.publisher.Mono
import xcj.app.userinfo.IpAddressFilter
import xcj.app.userinfo.ShareHandlerInterceptor

@Configuration(proxyBeanMethods = false)
class WebMvcConfig(
    private val shareHandlerInterceptor: ShareHandlerInterceptor,
    ): WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(shareHandlerInterceptor).addPathPatterns("/**")
    }

   /* override fun getValidator(): Validator? {
        CoreLogger.d("blue", "validator:$validator1")
        return validator1
    }
*/
}