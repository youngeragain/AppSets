package xcj.app.main.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import xcj.app.main.ShareHandlerInterceptor

@Configuration(proxyBeanMethods = false)
class WebMvcConfig(
    private val shareHandlerInterceptor: ShareHandlerInterceptor,
) : WebMvcConfigurer {

    companion object {
        private const val TAG = "WebMvcConfig"
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(shareHandlerInterceptor).addPathPatterns("/**")
    }

    /* override fun getValidator(): Validator? {
         CoreLogger.d("blue", "validator:$validator1")
         return validator1
     }
    */
}