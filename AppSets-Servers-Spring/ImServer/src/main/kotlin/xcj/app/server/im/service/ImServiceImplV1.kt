package xcj.app.server.im.service

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Service
import xcj.app.server.im.controller.Message
import xcj.app.server.im.dao.ImDao

@Service
class ImServiceImplV1 : IImService {
    @Autowired
    lateinit var imDao: ImDao

    @Autowired
    lateinit var rabbitTemplate: RabbitMessagingTemplate

    override fun saveImMessage() {

    }

    override fun saveImMessages() {

    }

    override fun getImMessages(): List<String> {
        return listOf("1", "2", "3", "4")
    }

    override fun sendMessage(message: Message) {
        message.content ?: return
        rabbitTemplate.send("one2one-fanout", message.routingKey ?: "", GenericMessage<String>(message.content ?: ""))
    }
}