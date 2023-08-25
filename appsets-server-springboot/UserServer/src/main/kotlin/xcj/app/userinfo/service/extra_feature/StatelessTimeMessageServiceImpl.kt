package xcj.app.userinfo.service.extra_feature

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.TimeMessage

@Service
class StatelessTimeMessageServiceImpl(
    private val stringRedisTemplate: StringRedisTemplate
) {
    private val TIME_MESSAGE_KEY = "TIME_MESSAGES"
    private val zSetOps = stringRedisTemplate.opsForZSet()
    fun addTimeMessage(timeMessage: TimeMessage):DesignResponse<Boolean>{
        val addResult = zSetOps.add(TIME_MESSAGE_KEY, timeMessage.content, System.currentTimeMillis().toDouble())
        return if(addResult==true){
            DesignResponse(data = true)
        }else{
            DesignResponse(code = ApiDesignCode.ERROR_CODE_FATAL, data = false)
        }
    }

    fun getStatelessTimeMessage(): DesignResponse<String?> {
        val randomMember = zSetOps.randomMember(TIME_MESSAGE_KEY)
        return DesignResponse(data = randomMember)
    }
}