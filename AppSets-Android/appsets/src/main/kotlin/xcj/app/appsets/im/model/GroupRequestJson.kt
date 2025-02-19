package xcj.app.appsets.im.model

data class GroupRequestJson(
    val requestId: String,
    val uid: String,
    val name: String?,
    var avatarUrl: String?,
    val hello: String,
    val groupId: String,
    val groupName: String,
    var groupIconUrl: String?
) : SystemContentInterface