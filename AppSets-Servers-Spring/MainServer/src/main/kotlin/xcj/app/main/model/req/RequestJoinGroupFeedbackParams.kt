package xcj.app.main.model.req

data class RequestJoinGroupFeedbackParams(
    val requestId: String,
    val requestUid: String,
    val groupId: String,
    val isAccept: Boolean,
    val userIds: List<String>,
    val addReason: String?
)