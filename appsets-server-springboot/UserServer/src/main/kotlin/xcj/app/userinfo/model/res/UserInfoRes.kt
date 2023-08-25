package xcj.app.userinfo.model.res

import java.io.Serializable

data class UserInfoRes(
    val agreeToTheAgreement:Int?,
    val uid:String,
    val name:String?,
    val age:String?,
    val sex:String?,
    val email:String?,
    val phone:String?,
    val address:String?,
    val avatarUrl:String?,
    val introduction:String?,
    val company:String?,
    val profession:String?,
    val website:String?
):Serializable
