package xcj.app.appsets.im

import xcj.app.appsets.im.message.IMMessage

data class Session(
    val imObj: IMObj,
    val conversationState: ConversationState
) {

    val id: String = imObj.id

    val latestIMMessage: IMMessage<*>?
        get() = conversationState.messages.firstOrNull()

    val isO2O: Boolean = imObj is IMObj.IMSingle

    val createTime = System.currentTimeMillis()

}