package xcj.app.main.model.req

data class DeleteFriendsParams(
    val friendUids: List<String>,
    val deleteReason: String?
)