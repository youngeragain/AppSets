package xcj.app.userinfo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import xcj.app.userinfo.util.*

@Configuration
class ContentCheckerConfiguration{
    @Bean
    fun contentCheckChainHolder(redisTemplate: StringRedisTemplate): ContentCheckChainHolder {
        val simpleKeywordsVerifier = SimpleKeywordsVerifier(redisTemplate)
        val x18ContentChecker = X18ContentChecker(redisTemplate)
        val realContentCheckChain = RealContentCheckChain()
        realContentCheckChain.setAddContentCheckers(listOf(simpleKeywordsVerifier, x18ContentChecker))
        return object:ContentCheckChainHolder{
            override fun getContentCheckChain(): ContentCheckChain {
                return realContentCheckChain
            }
        }
    }
}