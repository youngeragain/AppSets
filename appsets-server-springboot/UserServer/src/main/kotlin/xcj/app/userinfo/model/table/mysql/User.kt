package xcj.app.userinfo.model.table.mysql

import java.io.Serializable
import java.util.Date

data class User(
    val id:Int,
    val account:String,
    val password:String,
    val signUpTime:Date,
    val agreeToTheAgreement:Int,
    val canMultiOnline:Int,
    val uid:String,
    var salt:String?=null,
    var hash:String?=null
):Serializable
