package xcj.app.a.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import xcj.app.CoreLogger


@RequestMapping("/appsets")
@Controller
class MainController {

    @RequestMapping("page/login")
    fun loginPage():String {
        CoreLogger.d("blue", "MainController::loginPage")
        return "login"
    }
    @ResponseBody
    @RequestMapping("login")
    fun login(@RequestBody loginReq: LoginReq):String {
        CoreLogger.d("blue", "MainController::login:$loginReq")
        return "login result account:$loginReq"
    }

}
data class LoginReq(val account: String, val password: String)