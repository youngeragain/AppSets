package xcj.app.appsets.im

import androidx.compose.runtime.mutableStateListOf
import xcj.app.appsets.im.message.ImMessage

class ConversationState(
    initialMessages: List<ImMessage> = emptyList<ImMessage>()
) {
    private val _messages: MutableList<ImMessage> =
        mutableStateListOf(*initialMessages.toTypedArray())

    val messages: List<ImMessage> = _messages

    fun addMessage(message: ImMessage) {
        addOrUpdateMessage(message)
    }

    private fun addOrUpdateMessage(message: ImMessage) {
        val existMessage = _messages.firstOrNull { it.id == message.id }
        if (existMessage != null) {
            val index = _messages.indexOf(existMessage)
            _messages.remove(existMessage)
            _messages.add(index, message)
        } else {
            _messages.add(0, message)
        }
    }

    fun addMessages(messages: List<ImMessage>) {
        messages.forEach {
            addOrUpdateMessage(it)
        }
    }

    fun removeMessage(message: ImMessage) {
        _messages.removeIf { it.id == message.id }
    }
}