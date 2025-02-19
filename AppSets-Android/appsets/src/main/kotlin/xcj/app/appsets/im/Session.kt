package xcj.app.appsets.im

import xcj.app.appsets.im.message.ImMessage

data class Session(
    val imObj: ImObj,
    val conversationState: ConversationState
) {

    val id: String = imObj.id

    val latestImMessage: ImMessage?
        get() = conversationState.messages.firstOrNull()

    val isO2O: Boolean = imObj is ImObj.ImSingle

    val createTime = System.currentTimeMillis()

}