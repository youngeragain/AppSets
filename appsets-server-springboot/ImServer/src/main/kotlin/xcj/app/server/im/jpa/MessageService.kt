package xcj.app.server.im.jpa

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import xcj.app.server.im.controller.Message

@Service("jpaImMessageService")
class MessageService{
    @Autowired
    lateinit var messageRepository: MessageRepository

    fun getAll(): List<Message> {
        return messageRepository.findAll()
    }

    fun save(message: Message) {
        println("${System.currentTimeMillis()} received msg:$${message.content}, and saving")
        messageRepository.save(message)
    }
    fun saveBatch(message: List<Message>){
        messageRepository.saveAll(message)
    }
}