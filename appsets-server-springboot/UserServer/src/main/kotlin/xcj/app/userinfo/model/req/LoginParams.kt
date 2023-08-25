package xcj.app.userinfo.model.req

import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotNull


data class LoginParams(
    @get: NotNull(message = "account is required")
    var account:String? = null,
    @get: NotNull(message = "password is required")
    var password:String? = null,


    @get:NotNull(message = "signInDeviceInfo is required")
    @Valid
    val signInDeviceInfo:DeviceInfo?,
    val signInLocation:String?){
    fun compareToOtherLoginParams(loginParams: LoginParams):Boolean{
        return account==loginParams.account&&
                signInDeviceInfo?.platform==loginParams.signInDeviceInfo?.platform&&
                signInDeviceInfo?.screenResolution==loginParams.signInDeviceInfo?.screenResolution&&
                signInDeviceInfo?.screenSize==loginParams.signInDeviceInfo?.screenSize&&
                signInDeviceInfo?.version==loginParams.signInDeviceInfo?.version&&
                signInDeviceInfo?.vendor==loginParams.signInDeviceInfo?.vendor&&
                signInDeviceInfo?.model==loginParams.signInDeviceInfo?.model&&
                signInDeviceInfo?.modelCode==loginParams.signInDeviceInfo?.modelCode&&
                signInDeviceInfo?.ip==loginParams.signInDeviceInfo?.ip&&
                signInLocation==loginParams.signInLocation


    }
}


data class SignupParams(
    @get: NotNull(message = "account is required")
    val account:String,
    @get: NotNull(message = "password is required")
    val password:String,

    @get: NotNull(message = "name is required")
    val name:String,

    @get: NotNull(message = "avatarUrl is required")
    val avatarUrl:String,

    val introduction:String? = null,

    val tags:String? = null,


    //@get: NotNull(message = "sex is required")
    val sex:String? = null,

    //@get: NotNull(message = "age is required")
    //@get: Max(150)
    val age:Int? = null,
    val phone:String? = null,
    val email:String? = null,
    val area:String? = null,
    val address:String? = null,
    val website:String? = null,
    )
