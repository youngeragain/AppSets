package xcj.app.userinfo.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import xcj.app.DesignResponse
import xcj.app.userinfo.qr.MyCipherComponent
import xcj.app.userinfo.qr.StringBody


@RestController
class CipherController {
    @Autowired
    lateinit var myCipherComponent: MyCipherComponent

    @PostMapping("/cipher/aes/encode")
    fun encodeWithAes(@RequestBody body: StringBody): DesignResponse<StringBody> {
        return DesignResponse(data = StringBody(myCipherComponent.encode(body.text)))
    }

    @PostMapping("/cipher/aes/decode")
    fun decodeWithAes(@RequestBody body: StringBody): DesignResponse<StringBody> {
        return DesignResponse(data = StringBody(myCipherComponent.decode(body.text)))
    }
}