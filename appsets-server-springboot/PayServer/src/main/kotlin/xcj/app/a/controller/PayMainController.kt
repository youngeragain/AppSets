package xcj.app.a.controller

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.DesignResponse
import xcj.app.PayApi


@RequestMapping("/pay")
@RestController
class PayMainController: PayApi<String> {

    @RequestMapping("/order/{orderNo}")
    override fun payOder(@PathVariable orderNo: String):DesignResponse<String> {
        println("abcxxxxxxxxxxxxxxx")
        return DesignResponse<String>(data = "", info = "pay successful!, orderNo is:$orderNo")
    }

}