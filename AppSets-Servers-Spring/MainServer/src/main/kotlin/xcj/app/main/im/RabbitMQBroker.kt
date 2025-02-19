package xcj.app.main.im

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownSignalException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.amqp.core.*
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import xcj.app.main.model.table.mysql.appSetsUserAdmin0
import xcj.app.util.PurpleLogger
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class RabbitMQBrokerProperty(
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val virtualHost: String,
    val queuePrefix: String,
    val routingKeyPrefix: String,
    val groupExchangePrefix: String,
    val groupExchangeParent: String,
    val groupRootExchange: String,
    val groupSubRootExchange: String,
)

class RabbitMQBroker(
    amqpAdmin: AmqpAdmin,
    private val rabbitMessagingTemplate: RabbitMessagingTemplate,
    private val property: RabbitMQBrokerProperty
) : MessageBroker, Consumer {

    companion object {
        private const val TAG = "RabbitMQBroker"
    }

    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + EmptyCoroutineContext)

    init {
        val appSetsUserAdmin0 = appSetsUserAdmin0()
        val queueName = "user_" + appSetsUserAdmin0.uid + "_CUUID_" + UUID.randomUUID()
        val queue = Queue(queueName, false, true, false, null)
        amqpAdmin.declareQueue(queue)
        PurpleLogger.current.d(TAG, "init, queueName:$queueName")
        val imO2MFanOutSubRootExchange =
            ExchangeBuilder.fanoutExchange(property.groupSubRootExchange).durable(true).build<FanoutExchange>()
        amqpAdmin.declareExchange(imO2MFanOutSubRootExchange)
        val queueBindToExchange2 = BindingBuilder.bind(queue).to(imO2MFanOutSubRootExchange)
        amqpAdmin.declareBinding(queueBindToExchange2)

        rabbitMessagingTemplate.rabbitTemplate.execute { channel ->
            channel.basicConsume(queue.name, true, this)
        }
    }

    override fun onReceivedMessage(imMessage: ImMessage) {
        PurpleLogger.current.d(TAG, "onReceivedMessage, imMessage:${imMessage}")
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun sendMessage(imMessage: ImMessage) {
        coroutineScope.launch {
            PurpleLogger.current.d(TAG, " sendMessage")
            val properties = MessagePropertiesBuilder.newInstance().apply {
                setTimestamp(imMessage.timestamp)
                setType(ImMessageDesignType.getTypeByImMessage(imMessage))
                val headers = mutableMapOf<String, Any?>()
                headers[ImMessage.HEADER_MESSAGE_MESSAGE_DELIVERY_TYPE] = "RelayTransmission"
                headers[ImMessage.HEADER_MESSAGE_ID] = imMessage.id
                headers[ImMessage.HEADER_MESSAGE_UID] = imMessage.fromInfo.uid
                headers[ImMessage.HEADER_MESSAGE_NAME] = imMessage.fromInfo.name
                headers[ImMessage.HEADER_MESSAGE_NAME_BASE64] =
                    imMessage.fromInfo.name?.toByteArray()?.let(Base64::encode)
                headers[ImMessage.HEADER_MESSAGE_AVATAR_URL] = imMessage.fromInfo.avatarUrl
                headers[ImMessage.HEADER_MESSAGE_ROLES] = imMessage.fromInfo.roles
                if (!imMessage.messageGroupTag.isNullOrEmpty()) {
                    headers[ImMessage.HEADER_MESSAGE_MESSAGE_GROUP_TAG] = imMessage.messageGroupTag
                }
                if (imMessage.toInfo.id.isNotEmpty()) {
                    headers[ImMessage.HEADER_MESSAGE_TO_ID] = imMessage.toInfo.id
                }
                if (!imMessage.toInfo.name.isNullOrEmpty()) {
                    headers[ImMessage.HEADER_MESSAGE_TO_NAME] = imMessage.toInfo.name
                    headers[ImMessage.HEADER_MESSAGE_TO_NAME_BASE64] =
                        imMessage.toInfo.name?.let {
                            Base64.encode(it.toByteArray())
                        }
                }
                if (imMessage.toInfo.toType.isNotEmpty()) {
                    headers[ImMessage.HEADER_MESSAGE_TO_TYPE] = imMessage.toInfo.toType
                }
                if (!imMessage.toInfo.iconUrl.isNullOrEmpty()) {
                    headers[ImMessage.HEADER_MESSAGE_TO_ICON_URL] = imMessage.toInfo.iconUrl
                }
                if (!imMessage.toInfo.roles.isNullOrEmpty()) {
                    headers[ImMessage.HEADER_MESSAGE_TO_ROLES] = imMessage.toInfo.roles
                }
                copyHeaders(headers)
            }.build()
            val contentBytes =
                ImMessageGenerator.makeMessageMetadataAsJsonString(imMessage).toByteArray()
            val message = Message(contentBytes, properties)
            val exchange = property.groupExchangeParent
            val routingKey = "msg.to.group_${imMessage.toInfo.id}"
            PurpleLogger.current.d(TAG, "sendMessage, exchange:${exchange} routingKey:${routingKey}")
            runCatching {
                rabbitMessagingTemplate.rabbitTemplate.send(exchange, routingKey, message)
            }.onFailure {
                PurpleLogger.current.d(TAG, "sendMessage failed, ${it.message}")
            }
        }
    }

    private fun routingMessageToUserSelfClients(
        imMessage: ImMessage,
        basicProperties: AMQP.BasicProperties?,
        body: ByteArray?
    ) {
        if (basicProperties == null) {
            PurpleLogger.current.d(TAG, "routingMessageToUserSelfClients, basicProperties null, return!")
            return
        }
        if (imMessage.toInfo.toType != MessageToInfo.TO_TYPE_O2O) {
            PurpleLogger.current.d(TAG, "routingMessageToUserSelfClients, MessageToInfo is not one2one, return!")
            return
        }
        if (imMessage.fromInfo.uid == imMessage.toInfo.id) {
            PurpleLogger.current.d(TAG, "routingMessageToUserSelfClients, fromUid equals toId, return!")
            return
        }
        val properties = MessagePropertiesBuilder.newInstance().apply {
            basicProperties.timestamp?.let(::setTimestamp)
            basicProperties.type?.let(::setType)
            basicProperties.contentType?.let(::setContentType)
            basicProperties.headers?.let(::copyHeaders)
        }.build()
        val message = Message(body, properties)
        val exchange = property.groupExchangeParent
        val routingKey = "msg.to.group_${imMessage.fromInfo.uid}"
        PurpleLogger.current.d(TAG, "routingMessageToUserSelfClients, exchange:${exchange} routingKey:${routingKey}")
        kotlin.runCatching {
            rabbitMessagingTemplate.rabbitTemplate.send(exchange, routingKey, message)
        }.onFailure {
            PurpleLogger.current.d(TAG, "routingMessageToUserSelfClients failed, ${it.message}")
        }

    }

    override fun handleConsumeOk(consumerTag: String?) {
        PurpleLogger.current.d(TAG, " handleConsumeOk, consumerTag:${consumerTag}")
    }

    override fun handleCancelOk(consumerTag: String?) {
        PurpleLogger.current.d(TAG, " handleCancelOk, consumerTag:${consumerTag}")
    }

    override fun handleCancel(consumerTag: String?) {
        PurpleLogger.current.d(TAG, " handleCancel, consumerTag:${consumerTag}")
    }

    override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
        PurpleLogger.current.d(TAG, " handleShutdownSignal, consumerTag:${consumerTag}, sig:${sig?.message}")
    }

    override fun handleRecoverOk(consumerTag: String?) {
        PurpleLogger.current.d(TAG, " handleRecoverOk, consumerTag:${consumerTag}")
    }

    override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope?,
        properties: AMQP.BasicProperties?,
        body: ByteArray?
    ) {
        coroutineScope.launch {
            ImMessageGenerator.generateByReceived(properties, body)?.let { imMessage ->
                onReceivedMessage(imMessage)
                routingMessageToUserSelfClients(imMessage, properties, body)
            }
        }
    }
}