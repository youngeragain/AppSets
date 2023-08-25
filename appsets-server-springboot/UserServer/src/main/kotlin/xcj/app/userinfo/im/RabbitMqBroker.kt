package xcj.app.userinfo.im

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessagePropertiesBuilder
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import kotlin.coroutines.EmptyCoroutineContext

class RabbitMqBroker(private val rabbitMessagingTemplate: RabbitMessagingTemplate):MessageBroker{

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + EmptyCoroutineContext)
    override fun sendMessage(imMessage: ImMessage){

        coroutineScope.launch {
            runCatching {
                val properties = MessagePropertiesBuilder.newInstance().apply {
                    setTimestamp(imMessage.date)
                    setType(RabbitMqBrokerPropertyDesignType.getTypeByImMessage(imMessage))
                    setContentType(imMessage.contentType)
                    val headers = mutableMapOf<String, Any?>()
                    headers["messageDeliveryType"] = "RelayTransmission"
                    headers["msgId"] = imMessage.id
                    headers["uid"] = imMessage.msgFromInfo.uid
                    headers["name"] = imMessage.msgFromInfo.name
                    headers["avatarUrl"] = imMessage.msgFromInfo.avatarUrl
                    headers["roles"] = imMessage.msgFromInfo.roles
                    if (!imMessage.groupMessageTag.isNullOrEmpty())
                        headers["groupMessageTag"] = imMessage.groupMessageTag
                    if (imMessage.msgToInfo.id.isNotEmpty())
                        headers["toId"] = imMessage.msgToInfo.id
                    if (!imMessage.msgToInfo.name.isNullOrEmpty())
                        headers["toName"] = imMessage.msgToInfo.name
                    if (imMessage.msgToInfo.toType.isNotEmpty())
                        headers["toType"] = imMessage.msgToInfo.toType
                    if (!imMessage.msgToInfo.iconUrl.isNullOrEmpty())
                        headers["toIconUrl"] = imMessage.msgToInfo.iconUrl
                    if (!imMessage.msgToInfo.roles.isNullOrEmpty())
                        headers["toRoles"] = imMessage.msgToInfo.roles
                    copyHeaders(headers)
                }.build()
                val exchange = "one2one-fanout"
                val routingKey = "msg.user_"+imMessage.msgToInfo.id
                //给其他人发送
                val contentBytes = imMessage.content.toByteArray()
                val message = Message(contentBytes, properties)
                println("RabbitMqBroker"+ " sendMessage "+"exchange:${exchange} routingKey:${routingKey}")
                rabbitMessagingTemplate.rabbitTemplate.send(exchange, routingKey, message)
            }
        }
    }
}