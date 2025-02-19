package xcj.app.appsets.im.model

data class FriendRequestFeedbackJson(
    override val requestId: String,
    override val isAccept: Boolean
) : RequestFeedbackJson