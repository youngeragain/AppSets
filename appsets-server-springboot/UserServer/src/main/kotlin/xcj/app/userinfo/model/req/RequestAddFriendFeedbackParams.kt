package xcj.app.userinfo.model.req

data class RequestAddFriendFeedbackParams(
    val requestId:String,
    val requestUid:String,
    val isAccept:Boolean,
    val addReason:String?)