package xcj.app.userinfo.im

data class MessageFromInfo(
    val uid: String,
    val name: String? = uid,
    val avatarUrl: String? = null,
    val roles: String? = null
) {
    //ImMessage是否为系统发出的消息，非用户间的普通消息，规则是根据uid判断
    val isSystem: Boolean
        get() {
            roles ?: return false
            return roles.contains("system")
        }
}