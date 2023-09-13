package xcj.app.userinfo.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission

@RestController
class RabbitPropertyController {

    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.developerAdminStrToMd5)
    @GetMapping("/build/get_rabbit_properties")
    fun getRabbitPropertyV2():String? {
        val str = redisTemplate.opsForValue().get("rabbitproperty_for_app_build_use")
        if(str.isNullOrEmpty())
            return null
        val encodeToString = java.util.Base64.getEncoder().encodeToString(str.toByteArray())
        return encodeToString
    }
}