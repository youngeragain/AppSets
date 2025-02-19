package xcj.app.main.model.req

data class RequestAddFriendParams(
    val uid: String,
    val hello: String,
    val reason: String?
)