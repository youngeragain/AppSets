package xcj.app.main.model.res

data class MediaContentRes(
    val uri: String,
    val companionUri: String?,
    val relateUser: UserInfoRes?,
    val relateUserScreen: UserScreenRes?,
    val extraInfo: String?,
    val name: String?,
    val views: Int,
)