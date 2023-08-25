package xcj.app.userinfo.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RabbitPropertyController {

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate
    @GetMapping("/getrabbitproperties")
    fun getRabbitProperty():String? {
        return redisTemplate.opsForValue().get("rabbitproperty_for_app_build_use")
    }
}