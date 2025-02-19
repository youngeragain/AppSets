package xcj.app.server.im.service

import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import org.springframework.messaging.support.GenericMessage
import org.springframework.stereotype.Service
import xcj.app.server.im.controller.Message
import xcj.app.server.im.dao.ImDao

@Service
class ImServiceImplV2(
    val imDao: ImDao,
    val rabbitTemplate: RabbitMessagingTemplate
) : IImService {

    override fun saveImMessage() {

    }

    override fun saveImMessages() {

    }

    override fun getImMessages(): List<String> {
        return listOf("a", "b", "c", "d")
    }

    override fun sendMessage(message: Message) {
        message.content ?: return
        rabbitTemplate.send("one2one-fanout", message.routingKey ?: "", GenericMessage<String>(message.content ?: ""))
    }
}