package xcj.app.server.im.controller

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import xcj.app.server.im.jpa.MessageService
import xcj.app.server.im.service.ImServiceInterface
import xcj.app.server.im.service.ImServiceProxy
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id


@RequestMapping("/im")
@Controller
class ImController(
    @Qualifier("imServiceProxy") imService: ImServiceInterface,
    val messageService: MessageService
){

    private val imService1 = imService as ImServiceProxy

    @ResponseBody
    @RequestMapping("/messages/{type}")
    fun getImMessages(@PathVariable(name = "type") type:Int):List<Message>{
        return messageService.getAll()
    }

    @ResponseBody
    @RequestMapping("/message/send/{type}", method = [RequestMethod.POST, RequestMethod.GET])
    fun sendMessage(@RequestBody message: Message, @PathVariable(name = "type") type: Int):String{
        imService1.apply { this.type = type }.sendMessage(message)
        return "ok"
    }

}

@Entity
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id:Long?,
    var to_:String?=null,
    var content:String?=null,
    val payload:String?=null,
    val type_: Int?=null,
    @Transient
    val routingKey:String?=null,) {
    constructor() : this(null, null, null, null, null, null)
}