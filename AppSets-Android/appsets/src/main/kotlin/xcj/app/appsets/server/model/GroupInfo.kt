package xcj.app.appsets.server.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.ImSessionHolder
import xcj.app.appsets.im.Session

@Entity("GroupInfo")
data class GroupInfo(
    /**
     * @hidden
     */
    @PrimaryKey
    @ColumnInfo("group_id")
    var groupId: String,
    override var name: String? = null,
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
) : Bio, ImSessionHolder {

    override val id: String
        get() = groupId

    @Ignore
    override var bioUrl: Any? = null

    @Ignore
    override var imSession: Session? = null

    constructor() : this(
        "G0", Int.MIN_VALUE.toString(),
        null, null, null, null, null, null, null
    )

    companion object {

        fun basic(groupId: String, name: String?, iconUrl: String?): GroupInfo {
            return GroupInfo(groupId = groupId, name = name, iconUrl = iconUrl)
        }

    }
}


