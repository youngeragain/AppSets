package xcj.app.appsets.server.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.ImSessionHolder
import xcj.app.appsets.im.Member
import xcj.app.appsets.im.Session

@Entity("GroupInfo")
data class GroupInfo(
    var name: String? = null,
    @PrimaryKey
    @ColumnInfo("group_id")
    var groupId: String,
    @ColumnInfo("current_owner_uid")
    var currentOwnerUid: String? = null,
    var type: Int? = null,
    @ColumnInfo("icon_url")
    var iconUrl: String? = null,

    var introduction: String? = null,
    var public: Int? = null,
    @ColumnInfo("max_members")
    var maxMembers: Int? = null,

    @Ignore
    var userInfoList: MutableList<UserInfo>? = null,
) : ImSessionHolder {


    @Ignore
    override var imSession: Session? = null

    override fun asImSingle(): ImObj.ImSingle? = null
    override fun asImGroup(): ImObj.ImGroup {
        return ImObj.ImGroup(groupId, name, iconUrl).apply {
            users = userInfoList?.let { it as? MutableList<Member> }
        }
    }

    constructor() : this(
        null, Int.MIN_VALUE.toString(),
        null, null, null, null, null, null, null
    )

    companion object {
        fun basicInfo(groupId: String, name: String?, iconUrl: String?): GroupInfo {
            return GroupInfo(groupId = groupId, name = name, iconUrl = iconUrl)
        }
    }
}


