package xcj.app.main.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.StringRedisTemplate
import xcj.app.main.util.*

@Configuration
class ContentCheckerConfiguration {
    companion object {
        private const val TAG = "ContentCheckerConfiguration"
    }

    @Bean
    fun contentCheckChainHolder(redisTemplate: StringRedisTemplate): ContentCheckChainHolder {
        val simpleKeywordsVerifier = SimpleKeywordsVerifier(redisTemplate)
        val restrictedContentChecker = RestrictedContentChecker(redisTemplate)
        val realContentCheckChain = RealContentCheckChain()
        realContentCheckChain.setAddContentCheckers(listOf(simpleKeywordsVerifier, restrictedContentChecker))
        return object : ContentCheckChainHolder {
            override fun getContentCheckChain(): ContentCheckChain {
                return realContentCheckChain
            }
        }
    }
}