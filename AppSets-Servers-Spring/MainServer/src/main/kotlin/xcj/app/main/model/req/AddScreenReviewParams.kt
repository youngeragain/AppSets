package xcj.app.main.model.req

data class AddScreenReviewParams(
    val screenId: String,
    val content: String,
    val isPublic: Boolean,
    val screenReviewId: String?
)