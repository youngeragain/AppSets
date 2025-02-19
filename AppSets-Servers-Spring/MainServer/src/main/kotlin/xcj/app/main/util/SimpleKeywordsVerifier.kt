package xcj.app.main.util

import org.springframework.data.redis.core.StringRedisTemplate
import xcj.app.util.PurpleLogger

class SimpleKeywordsVerifier(private val redisTemplate: StringRedisTemplate) : ContentChecker {

    companion object {
        private const val TAG = "SimpleKeywordsVerifier"
    }

    override fun <I> check(input: I?): Boolean {
        PurpleLogger.current.d(TAG, "check")
        if (input == null || input !is String)
            return true
        if (!redisTemplate.hasKey("Content_KeyWords_Of_Swearing")) {
            return true
        }
        return !(redisTemplate.opsForSet().members("Content_KeyWords_Of_Swearing")?.contains(input) ?: true)
    }

    override fun <I, O> transform(input: I?): O? {
        PurpleLogger.current.d(TAG, "transform")
        return input as? O
    }
}

