package xcj.app.main.util

import org.springframework.data.redis.core.StringRedisTemplate
import xcj.app.util.PurpleLogger

class RestrictedContentChecker(
    private val redisTemplate: StringRedisTemplate
) : ContentChecker {

    companion object{
        private val TAG = "RestrictedContentChecker"
    }



    override fun <I> check(input: I?): Boolean {
        PurpleLogger.current.d(TAG, "check")
        if (input == null || input !is String)
            return true
        if (!redisTemplate.hasKey("Content_KeyWords_Of_X_18"))
            return true
        return !(redisTemplate.opsForSet().members("Content_KeyWords_Of_X_18")?.contains(input) ?: false)
    }

    override fun <I, O> transform(input: I?): O? {
        PurpleLogger.current.d(TAG, "transform")
        return input as? O
    }
}