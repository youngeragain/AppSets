package xcj.app.server.im.listeners

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import xcj.app.server.im.controller.Message
import xcj.app.server.im.jpa.MessageService
import xcj.app.util.PurpleLogger




@Component
class ImListener(messageService: MessageService) {
    val holder = MessagesTempHolder(messageService)

    @RabbitListener(queues = ["im-sever-queue-for-saving"])
    fun onMessage(msg: String) {
        holder.saveBeforeCheck(msg)
    }
}