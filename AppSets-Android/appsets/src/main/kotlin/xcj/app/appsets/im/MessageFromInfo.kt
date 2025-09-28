package xcj.app.appsets.im

import xcj.app.appsets.server.model.UserRole

data class MessageFromInfo(
    val uid: String,
    val name: String?,
    val avatarUrl: String? = null,
    val roles: String? = null
) : Bio {
    override val bioId: String
        get() = uid
    override val bioName: String?
        get() = name ?: uid
    override var bioUrl: Any? = null

    //ImMessage是否为系统发出的消息，非用户间的普通消息，规则是根据uid判断
    val isSystem: Boolean
        get() {
            return roles?.contains(UserRole.ROLE_ADMIN) == true
        }
}