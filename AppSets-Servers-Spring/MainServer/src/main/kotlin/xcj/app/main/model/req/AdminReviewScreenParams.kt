package xcj.app.main.model.req

data class AdminReviewScreenParams(
    val screenId: String,
    val reviewResult: Int,
    val reviewMessage: String
)