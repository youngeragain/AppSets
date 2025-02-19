package xcj.app.server.im.controller

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import xcj.app.DesignResponse
import xcj.app.server.im.jpa.MessageService
import xcj.app.server.im.service.ImServiceImplV1
import xcj.app.server.im.service.IImServiceProxy
import xcj.app.server.im.model.Message
import xcj.app.server.im.service.IImService


@RequestMapping("/im")
@Controller
class ImController(
    @Qualifier("imServiceProxy") imService: IImService,
    val messageService: MessageService
) {

    private val imServiceProxy: IImServiceProxy = imService as IImServiceProxy

    @ResponseBody
    @RequestMapping("/messages/{type}")
    fun getImMessages(@PathVariable(name = "type") type: Int): DesignResponse<List<Message>> {
        return DesignResponse(data = messageService.getAll())
    }

    @ResponseBody
    @RequestMapping("/message/send/{type}", method = [RequestMethod.POST, RequestMethod.GET])
    fun sendMessage(@RequestBody message: Message, @PathVariable(name = "type") type: Int): DesignResponse<Boolean> {
        imServiceProxy.apply { this.type = type }.sendMessage(message)
        return DesignResponse(data = true)
    }

}