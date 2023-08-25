package xcj.app.userinfo.model.req

data class AddScreenReviewParams(
    val screenId:String,
    val content:String,
    val isPublic:Boolean,
    val screenReviewId:String?
)