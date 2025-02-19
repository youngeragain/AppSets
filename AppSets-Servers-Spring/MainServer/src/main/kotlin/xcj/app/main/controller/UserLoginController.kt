package xcj.app.main.controller

import org.springframework.validation.BindingResult
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.ktx.produceExceptionIfBindingResultContains
import xcj.app.main.model.req.LoginParams
import xcj.app.main.model.req.SignupParams
import xcj.app.main.model.req.TokenTradeInParams
import xcj.app.main.model.table.mongo.User
import xcj.app.main.service.UserLoginService

@RequestMapping("/user")
@RestController
class UserLoginController(
    private val loginService: UserLoginService
) {

    /**
     * token过期后以旧换新
     */
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.LoginRequired(ApiDesignPermission.LoginRequired.TOKEN_STATE_VALID_HAS_EXPIRED)
    @RequestMapping("token/trade-in")
    fun tokenTradeIn(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @Validated @RequestBody tokenTradeInParams: TokenTradeInParams
    ): DesignResponse<String> {
        return loginService.tokenTradeIn(token, tokenTradeInParams)
    }

    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("login", method = [RequestMethod.POST])
    fun logIn(
        @Validated @RequestBody loginParams: LoginParams,
        bindingResult: BindingResult
    ): DesignResponse<String> {
        bindingResult.produceExceptionIfBindingResultContains()
        return loginService.logIn(loginParams)
    }

    /**
     * 已登录设备给另外设备登录使用
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("login2", method = [RequestMethod.POST])
    fun logInByOtherDevice(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @Validated @RequestBody loginParams: LoginParams
    ): DesignResponse<String> {
        return loginService.logInByOtherDevice(token, loginParams)
    }

    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("signup", method = [RequestMethod.POST])
    fun signUp(
        @Validated @RequestBody signupParams: SignupParams
    ): DesignResponse<Boolean> {
        return loginService.signUp(signupParams)
    }

    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("signup/pre", method = [RequestMethod.POST])
    fun preSignUp(
        @RequestParam("ac", required = true) account: String
    ): DesignResponse<Boolean> {
        return loginService.preSignUp(account)
    }


    @ApiDesignPermission.LoginRequired
    @RequestMapping("signout", method = [RequestMethod.GET])
    fun signOut(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<Boolean> {
        return loginService.signOut(token)
    }

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.SpecialRequired("200")
    @RequestMapping("logout/{deleteData}")
    fun logOut(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "deleteData") deleteData: Boolean
    ): DesignResponse<String> {
        return loginService.logOut(token, deleteData)
    }

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/logout/{userId}/{deleteData}")
    fun logOutAdmin(
        @PathVariable(name = "deleteData") deleteData: Boolean,
        @PathVariable(name = "userId") userId: String
    ): DesignResponse<String> {
        return loginService.logOutByUserId(userId, deleteData)
    }

    @RequestMapping("mongo/add", method = [RequestMethod.POST])
    fun addUserToMongo(
        @RequestBody user: User
    ): DesignResponse<String> {
        return loginService.addUserToMongo(user)
    }

}