package xcj.app.appsets.im.model

data class GroupJoinRequestFeedbackJson(
    override val requestId: String,
    override val isAccept: Boolean
) : RequestFeedbackJson