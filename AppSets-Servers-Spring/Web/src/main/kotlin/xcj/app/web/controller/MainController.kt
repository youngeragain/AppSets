package xcj.app.web.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import xcj.app.web.model.LoginParams
import xcj.app.util.PurpleLogger

@RequestMapping("/appsets")
@Controller
class MainController {

    companion object {
        private const val TAG = "MainController"
    }

    @RequestMapping("page/login")
    fun loginPage(): String {
        PurpleLogger.current.d(TAG, "MainController::loginPage")
        return "login"
    }

    @ResponseBody
    @RequestMapping("login")
    fun login(@RequestBody loginParams: LoginParams): String {
        PurpleLogger.current.d(TAG, "MainController::login:$loginParams")
        return "login result account:$loginParams"
    }

}

