package xcj.app.appsets.im

import androidx.compose.runtime.mutableStateListOf

class ConversationUiState(
    initialMessages: List<ImMessage>
) {
    private val _messages: MutableList<ImMessage> =
        mutableStateListOf(*initialMessages.toTypedArray())
    val messages: List<ImMessage> = _messages

    fun addMessage(msg: ImMessage) {
        _messages.add(0, msg) // Add to the beginning of the list
    }

    fun addMessages(msgs: List<ImMessage>) {
        _messages.addAll(msgs)
    }

    fun removeMessage(message: ImMessage) {
        _messages.removeIf { it.id == message.id }
    }
}