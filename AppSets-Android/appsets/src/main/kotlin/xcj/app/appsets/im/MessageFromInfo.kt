package xcj.app.appsets.im

import xcj.app.appsets.server.model.UserRole

data class MessageFromInfo(
    val uid: String,
    override val name: String? = uid,
    val avatarUrl: String? = null,
    val roles: String? = null
) : Bio {
    override val id: String
        get() = uid
    override var bioUrl: Any? = null

    //ImMessage是否为系统发出的消息，非用户间的普通消息，规则是根据uid判断
    val isSystem: Boolean
        get() {
            return roles?.contains(UserRole.ROLE_ADMIN) == true
        }
}