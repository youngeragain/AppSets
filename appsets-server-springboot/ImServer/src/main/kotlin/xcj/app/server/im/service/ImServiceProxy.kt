package xcj.app.server.im.service

import org.springframework.stereotype.Service
import xcj.app.server.im.controller.Message

@Service
class ImServiceProxy(
    val imService1: ImServiceInterface,
    val imService: ImServiceInterface
):ImServiceInterface {
    var type:Int=0
    override fun saveImMessage() {
        find().saveImMessages()
    }

    private fun find():ImServiceInterface {
        return if(type==0){
            imService
        }else{
            imService1
        }
    }

    override fun saveImMessages() {
        find().saveImMessages()
    }

    override fun getImMessages(): List<String> {
        return find().getImMessages()
    }

    override fun sendMessage(message: Message) {
        find().sendMessage(message)
    }
}