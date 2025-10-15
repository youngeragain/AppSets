package xcj.app.appsets.im

import androidx.compose.runtime.mutableStateListOf
import xcj.app.appsets.im.message.IMMessage

class ConversationState(
    initialMessages: List<IMMessage<*>> = emptyList()
) {
    private val _messages: MutableList<IMMessage<*>> =
        mutableStateListOf(*initialMessages.toTypedArray())

    val messages: List<IMMessage<*>> = _messages

    fun addMessage(message: IMMessage<*>) {
        addOrUpdateMessage(message)
    }

    private fun addOrUpdateMessage(message: IMMessage<*>) {
        val existMessage = _messages.firstOrNull { it.id == message.id }
        if (existMessage != null) {
            val index = _messages.indexOf(existMessage)
            _messages.remove(existMessage)
            _messages.add(index, message)
        } else {
            _messages.add(0, message)
        }
    }

    fun addMessages(messages: List<IMMessage<*>>) {
        messages.forEach {
            addOrUpdateMessage(it)
        }
    }

    fun removeMessage(message: IMMessage<*>) {
        _messages.removeIf { it.id == message.id }
    }
}