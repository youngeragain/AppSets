package xcj.app.main.service.extra_feature

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.main.model.req.TimeMessage

@Service
class StatelessTimeMessageServiceImpl(
    stringRedisTemplate: StringRedisTemplate
) {
    companion object {
        private const val TAG = "StatelessTimeMessageServiceImpl"
        private const val KEY_TIME_MESSAGE = "TIME_MESSAGES"
    }

    private val zSetOps = stringRedisTemplate.opsForZSet()

    fun addTimeMessage(timeMessage: TimeMessage): DesignResponse<Boolean> {
        val addResult = zSetOps.add(KEY_TIME_MESSAGE, timeMessage.content, System.currentTimeMillis().toDouble())
        return if (addResult == true) {
            DesignResponse(data = true)
        } else {
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, data = false)
        }
    }

    fun getStatelessTimeMessage(): DesignResponse<String?> {
        val randomMember = zSetOps.randomMember(KEY_TIME_MESSAGE)
        return DesignResponse(data = randomMember)
    }
}