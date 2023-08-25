package xcj.app.userinfo.model.table.mysql

import java.io.Serializable

data class UserInfo(
    val name:String?,
    val age:String?,
    val sex:String?,
    val email:String?,
    val phone:String?,
    val uid:String?,
    val address:String?,
    val avatarUrl:String?,
    val intro:String?,
    val company:String?,
    val profession:String?,
    val website:String?
):Serializable


fun appSetsUserAdmin0():UserInfo{
    return UserInfo(
        name = "AppSets",
        age = "0",
        sex = "",
        email = "",
        phone = "",
        uid = "U0000000000000000000000",
        address = "",
        avatarUrl = "https://i.loli.net/2021/05/16/BGC5IMwrSKm72v4.png",
        intro = "",
        company = "",
        profession = "",
        website = ""
    )
}