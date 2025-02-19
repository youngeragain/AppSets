package xcj.app.main.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.client.RestTemplate
import xcj.app.main.util.TokenHelper

@Configuration(proxyBeanMethods = false)
class BeanProvider {

    companion object {
        private const val TAG = "BeanProvider"
    }

    @Bean
    fun tokenHelper(stringRedisTemplate: StringRedisTemplate): TokenHelper {
        return TokenHelper(stringRedisTemplate)
    }

    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.interceptors = listOf(RestTemplateLoggInterceptor())
        return restTemplate
    }
}
