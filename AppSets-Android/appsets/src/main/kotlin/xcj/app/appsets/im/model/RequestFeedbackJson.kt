package xcj.app.appsets.im.model

interface RequestFeedbackJson : SystemContentInterface {
    val requestId: String
    val isAccept: Boolean
}
