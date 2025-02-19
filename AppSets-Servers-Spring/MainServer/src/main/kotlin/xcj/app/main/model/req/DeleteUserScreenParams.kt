package xcj.app.main.model.req


data class DeleteUserScreenParams(
    val screenId: String,
    val addReason: String?
)