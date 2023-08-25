package xcj.app.userinfo.util

import org.springframework.data.redis.core.StringRedisTemplate
import xcj.app.CoreLogger

class X18ContentChecker(
    private val redisTemplate: StringRedisTemplate
):ContentChecker{
    private val TAG = "X18ContentChecker"
    override fun <I> check(input: I?): Boolean {
        CoreLogger.d(TAG, "check")
        if(input==null||input !is String)
            return true
        if(!redisTemplate.hasKey("Content_KeyWords_Of_X_18"))
            return true
        return !(redisTemplate.opsForSet().members("Content_KeyWords_Of_X_18")?.contains(input)?:false)
    }

    override fun <I, O> transform(input: I?): O? {
        CoreLogger.d(TAG, "transform")
        return input as? O
    }
}