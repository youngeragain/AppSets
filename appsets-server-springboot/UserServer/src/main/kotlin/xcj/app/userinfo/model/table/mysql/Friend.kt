package xcj.app.userinfo.model.table.mysql

import java.util.Date

data class Friend(
    val id:Int,
    val uid:String,
    val friendUid:String,
    val addReason:String,
    val addedTime:Date,
    val createTime:Date,
    val updateTime:Date)
