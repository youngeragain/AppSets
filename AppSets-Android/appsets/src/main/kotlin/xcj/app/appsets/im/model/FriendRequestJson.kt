package xcj.app.appsets.im.model

data class FriendRequestJson(
    val requestId: String,
    val uid: String,
    val name: String?,
    var avatarUrl: String?,
    val hello: String
) : SystemContentInterface