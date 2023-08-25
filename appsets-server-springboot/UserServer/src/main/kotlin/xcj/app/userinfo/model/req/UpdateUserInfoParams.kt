package xcj.app.userinfo.model.req

import xcj.app.userinfo.model.table.mysql.UserInfo


data class UpdateUserInfoParams(
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
    val website:String?){
    fun toUserInfo():UserInfo{
        uid?:throw Exception("uid is null! UpdateUserInfoParams can not to UserInfo")
        return UserInfo(
            name, age, sex, email, phone, uid, address, avatarUrl, intro, company, profession, website
        )
    }
}
