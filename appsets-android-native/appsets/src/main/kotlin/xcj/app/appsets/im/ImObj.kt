package xcj.app.appsets.im

import xcj.app.appsets.usecase.UserRelationsCase

sealed class ImObj(
    val id: String,
    val name: String,
    val avatar: String? = null,
    var isRelated: Boolean = false
) {


    data class ImSingle(
        var uid: String,
        val userName: String?,
        val userAvatarUrl: String?,
        val userRoles: String?
    ) :
        ImObj(
            uid, userName ?: uid, userAvatarUrl,
            UserRelationsCase.getInstance().hasUserRelated(uid)
        )

    data class ImGroup(
        val groupId: String,
        val groupName: String?,
        val groupAvatarUrl: String?,
    ) : ImObj(
        groupId, groupName ?: groupId, groupAvatarUrl,
        UserRelationsCase.getInstance().hasGroupRelated(groupId)
    ) {
        var users: MutableList<Member>? = null
    }

    /**
     * 用于在列表中显示朋友或者陌生人归类的辅助类
     */
    data class ImTitle(val title: String) : ImObj(Int.MIN_VALUE.toString(), title, null)
}


fun ImObj.toToInfo(): MessageToInfo {
    return when (this) {
        is ImObj.ImSingle -> {
            MessageToInfo("one2one", this.id, this.name, this.avatar, this.userRoles)
        }

        is ImObj.ImGroup -> {
            MessageToInfo("one2many", this.id, this.name, this.avatar, null)
        }

        is ImObj.ImTitle -> {
            throw Exception("ImTitle can't be transform to MessageToInfo!")
        }
    }
}