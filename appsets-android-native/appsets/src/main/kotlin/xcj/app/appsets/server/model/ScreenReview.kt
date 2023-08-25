package xcj.app.appsets.server.model

data class ScreenReview(
    val reviewId: String,
    val content: String,
    val reviewTime: String,
    val likes: Int,
    val dislikes: Int,
    val pid: String?,
    val uid: String,
    val screenId: String,
    val reviewPassed: Int,
    val withdraw: Int,
    var isPublic: Int,
    val userInfo: UserInfo?
)