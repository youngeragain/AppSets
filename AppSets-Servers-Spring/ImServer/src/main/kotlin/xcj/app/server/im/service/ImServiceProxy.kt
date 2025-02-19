package xcj.app.server.im.service

import org.springframework.stereotype.Service
import xcj.app.server.im.controller.Message

@Service
class IImServiceProxy(
    val imService1: ImServiceImplV1,
    val imService2: ImServiceImplV2
) : IImService {
    var type: Int = 0
    override fun saveImMessage() {
        find().saveImMessages()
    }

    private fun find(): IImService {
        return if (type == 0) {
            imService1
        } else {
            imService2
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