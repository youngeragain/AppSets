package xcj.app.server.im.jpa

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xcj.app.server.im.controller.Message
import xcj.app.server.im.model.Message
import xcj.app.util.PurpleLogger

@Service("jpaImMessageService")
class MessageService {

    companion object {
        private const val TAG = "MessageService"
    }

    @Autowired
    lateinit var messageRepository: MessageRepository

    fun getAll(): List<Message> {
        return messageRepository.findAll()
    }

    fun save(message: Message) {
        PurpleLogger.current.d(TAG, "save, ${System.currentTimeMillis()} received msg:$${message.content}, and saving")
        messageRepository.save(message)
    }

    fun saveBatch(message: List<Message>) {
        messageRepository.saveAll(message)
    }
}