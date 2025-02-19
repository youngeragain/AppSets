package xcj.app.main.model.res

import java.io.Serializable


data class GroupInfoRes(
    val name: String?,
    val groupId: String?,
    val currentOwnerUid: String?,
    val type: Int?,
    val public: Int?,
    val maxMembers: Int?,
    val iconUrl: String?,
    val introduction: String?,
    val userInfoList: List<UserInfoRes>?
) : Serializable {
    constructor() : this(
        null, null, null,
        null, null, null, null, null, null
    )
}