package xcj.app.main.model.table.mysql

import java.io.Serializable
import java.util.*

data class ChatGroup(
    val id: Int,
    val name: String,
    val groupId: String,
    val createUid: String,
    val currentOwnerUid: String,
    val lastOwnerUid: String,
    val type: Int,
    val iconUrl: String? = null,
    val introduction: String? = null,
    val createTime: Date,
    val updateTime: Date? = null
) : Serializable
