package xcj.app.userinfo.service

import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.LoginParams
import xcj.app.userinfo.model.req.SignupParams
import xcj.app.userinfo.model.req.TokenTradeInParams
import xcj.app.userinfo.model.table.mongo.User

interface UserLoginService {
    fun logIn(loginParams: LoginParams):DesignResponse<String>
    fun signOut(token: String):DesignResponse<Boolean>
    fun signUp(signupParams: SignupParams):DesignResponse<Boolean>
    fun logOut(token: String, deleteData:Boolean):DesignResponse<String>
    fun logOutByUserId(userId: String, deleteData:Boolean):DesignResponse<String>
    fun addUserToMongo(user: User):DesignResponse<String>
    fun tokenTradeIn(token: String, tokenTradeInParams: TokenTradeInParams):DesignResponse<String>
    fun logInByOtherDevice(token: String, loginParams: LoginParams): DesignResponse<String>
    fun preSignUp(account: String): DesignResponse<Boolean>
}

