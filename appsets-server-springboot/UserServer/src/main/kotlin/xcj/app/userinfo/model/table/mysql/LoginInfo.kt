package xcj.app.userinfo.model.table.mysql

data class LoginInfo(
    val uid:String,
    val signInDeviceInfo:String?,
    val signInLocation:String?,
    val signInIp:String?
)
