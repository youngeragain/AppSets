package xcj.app.main.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xcj.app.DesignResponse

@RequestMapping("/appsets/test")
@RestController
class TestController {

    @GetMapping("/info")
    fun showInformation(): DesignResponse<String> {
        return DesignResponse(data = "this is a sample information!")
    }

}