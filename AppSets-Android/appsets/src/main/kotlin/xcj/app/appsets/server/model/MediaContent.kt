package xcj.app.appsets.server.model

data class MediaContent(
    var uri: String,
    var companionUri: String?,
    val relateUser: UserInfo?,
    val relateUserScreen: ScreenInfo?,
    val extraInfo: String?,
    val name: String?,
    val views: Int
)

