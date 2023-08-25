package xcj.app.appsets.server.model

data class AddUserScreenParams(
    val screenContent: String?,
    val associateTopics: String?,
    val associateUsers: String?,
    val mediaFileUrls: List<ScreenMediaFileUrl>?,
    val isPublic: Boolean?,
    val addReason: String?
)