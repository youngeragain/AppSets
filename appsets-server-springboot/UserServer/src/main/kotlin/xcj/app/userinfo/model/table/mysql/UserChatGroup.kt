package xcj.app.userinfo.model.table.mysql

import java.util.*

data class UserChatGroup(
    val id:Int,
    val groupId:String,
    val uid:String,
    val createTime: Date,
    val updateTime:Date)
