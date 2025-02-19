package xcj.app.server.im.listeners

import xcj.app.server.im.controller.Message
import xcj.app.server.im.jpa.MessageService
import xcj.app.util.PurpleLogger

class MessagesTempHolder(private val messageService: MessageService) {

    companion object {
        private const val TAG = "MessagesTempHolder"
    }

    private val messages: MutableList<String> = mutableListOf()
    private var size = 0
    fun saveBeforeCheck(msg: String) {
        synchronized(messageService) {
            PurpleLogger.current.d(TAG, "saveBeforeCheck, current message holder size:$size")
            if (size + 1 > 5) {
                val subList = messages.subList(0, 5)
                val toSaveList = subList.map {
                    Message().apply { content = it }
                }
                messageService.saveBatch(toSaveList)
                messages.removeAll(subList)
                size -= 5
            } else {
                messages.add(msg)
                size = messages.size
            }
        }
    }
}