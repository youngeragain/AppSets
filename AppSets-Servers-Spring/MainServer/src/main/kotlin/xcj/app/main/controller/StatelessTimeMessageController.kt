package xcj.app.main.controller

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.DesignResponse
import xcj.app.main.model.req.TimeMessage
import xcj.app.main.service.extra_feature.StatelessTimeMessageServiceImpl

/**
 * 用Redis 实现一种类似漂流瓶，纸飞机的功能
 */
@RequestMapping("/appsets")
@RestController
class StatelessTimeMessageController(
    private val timeMessageServiceImpl: StatelessTimeMessageServiceImpl
) {

    @RequestMapping("feature/stateless-time-message/random")
    fun getStatelessTimeMessageRandom(): DesignResponse<String?> {
        return timeMessageServiceImpl.getStatelessTimeMessage()
    }

    @RequestMapping("feature/stateless-time-message/add")
    fun createStatelessTimeMessageRandom(@RequestBody timeMessage: TimeMessage): DesignResponse<Boolean> {
        return timeMessageServiceImpl.addTimeMessage(timeMessage)
    }
}