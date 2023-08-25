package xcj.app.server.im.listeners

import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import xcj.app.server.im.controller.Message
import xcj.app.server.im.jpa.MessageService

class MessagesTempHolder(val messageService: MessageService){
    val messages:MutableList<String> = mutableListOf()
    var size = 0
    fun saveBeforeCheck(msg: String){
        synchronized(messageService){
            println("current message holder size:$size")
            if(size+1>5){
                val subList = messages.subList(0, 5)
                val toSaveList = subList.map {
                    Message().apply { content = it }
                }
                messageService.saveBatch(toSaveList)
                messages.removeAll(subList)
                size-=5
            }else {
                messages.add(msg)
                size = messages.size
            }
        }
    }
}


@Component
class ImListener(messageService: MessageService) {
    val holder = MessagesTempHolder(messageService)
    @RabbitListener(queues = ["im-sever-queue-for-saving"])
    fun onMessage(msg:String){
        holder.saveBeforeCheck(msg)
    }
}