package xcj.app.userinfo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestTemplate
import xcj.app.CoreLogger
import xcj.app.userinfo.TokenHelper

@Configuration(proxyBeanMethods = false)
class BeanProvider {

    @Bean
    fun tokenHelper(stringRedisTemplate: StringRedisTemplate): TokenHelper {
        return TokenHelper(stringRedisTemplate)
    }
    @Bean
    fun restTemplate():RestTemplate{
        val restTemplate = RestTemplate()
        restTemplate.interceptors = listOf(RestTemplateLoggInterceptor())
        return restTemplate
    }
}
