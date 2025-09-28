package xcj.app.appsets.im

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BlockedListener
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import com.rabbitmq.client.Recoverable
import com.rabbitmq.client.RecoveryListener
import com.rabbitmq.client.impl.recovery.AutorecoveringConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.MessageSendInfo
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class RabbitMQBroker : MessageBroker<RabbitMQBrokerConfig>,
    DeliverCallback, CancelCallback,
    RecoveryListener, BlockedListener {

    companion object {
        private const val TAG = "RabbitMQBroker"
    }

    private lateinit var property: RabbitMQBrokerProperty

    private var connection: AutorecoveringConnection? = null

    private var channel: Channel? = null

    private var queueName: String? = null

    private var uid: String? = null

    private var brokerDeliveryMode = 2//1->non-persistent, 2->persistent

    fun isConnected(): Boolean {
        return checkConnection()
    }

    override suspend fun retry() {

    }

    /**
     * start point
     */
    override suspend fun bootstrap(config: RabbitMQBrokerConfig) {
        return updateUserChannel(config.getRabbitProperty())
    }

    private suspend fun updateUserChannel(property: RabbitMQBrokerProperty) {
        PurpleLogger.current.d(TAG, "updateUserChannel")
        withContext(Dispatchers.IO) {
            this@RabbitMQBroker.property = property
            val userGroupsChanged = true
            val loggedUserUid = LocalAccountManager.userInfo.uid
            val userChanged = uid != loggedUserUid
            uid = loggedUserUid
            basicConnect()
            handleUserChannel(userChanged, userGroupsChanged)
        }
    }

    suspend fun updateImGroupBindIfNeeded() {
        PurpleLogger.current.d(TAG, "updateImGroupBindIfNeeded")
        if (!::property.isInitialized) {
            return
        }
        updateUserChannel(property)
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            deleteUserQueue()
            closeConnection()
        }
    }

    private fun deleteUserQueue() {
        if (!checkConnection()) {
            return
        }
        if (queueName.isNullOrEmpty()) {
            return
        }
        runCatching {
            channel?.queueDelete(queueName)
        }.onSuccess {
            PurpleLogger.current.d(TAG, "deleteUserQueue, success")
        }.onFailure {
            PurpleLogger.current.d(TAG, "deleteUserQueue, failed")
        }
    }

    private fun closeConnection() {
        runCatching {
            connection?.removeBlockedListener(this)
            connection?.removeRecoveryListener(this)
            channel?.close()
            connection?.close()
            channel = null
            connection = null
            queueName = null
            uid = null
        }.onSuccess {
            BrokerTest.imOnLineState.value = IMOnlineState.Offline
            PurpleLogger.current.d(TAG, "closeConnection, success")
        }.onFailure {
            PurpleLogger.current.d(TAG, "closeConnection, failed")
        }
    }

    override fun handleBlocked(reason: String?) {
        PurpleLogger.current.d(TAG, "handleBlocked, reason:$reason")
    }

    override fun handleUnblocked() {
        PurpleLogger.current.d(TAG, "handleUnblocked")
    }

    override fun handleRecovery(recoverable: Recoverable?) {
        PurpleLogger.current.d(TAG, "handleRecovery")
        BrokerTest.imOnLineState.value = IMOnlineState.Online()
    }

    override fun handleRecoveryStarted(recoverable: Recoverable?) {
        PurpleLogger.current.d(TAG, "handleRecoveryStarted")
        BrokerTest.imOnLineState.value = IMOnlineState.Offline
    }

    private fun basicConnect() {
        if (checkConnection()) {
            PurpleLogger.current.d(
                TAG,
                "basicConnect! connection is connected, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "basicConnect, start, rabbit host:port: ${property.host}:${property.port}"
        )

        val factory = ConnectionFactory().apply {
            host = property.host
            port = property.port
            username = property.username
            password = property.password
            virtualHost = property.virtualHost
            useNio()
            setNetworkRecoveryInterval(500)//millis seconds, 0.5s
            //requestedHeartbeat = 10//seconds
        }
        runCatching {
            val autoRecoveringConnection = factory.newConnection() as AutorecoveringConnection
            autoRecoveringConnection.addRecoveryListener(this)
            autoRecoveringConnection.addBlockedListener(this)
            connection = autoRecoveringConnection
            channel = autoRecoveringConnection.createChannel()
        }.onSuccess {
            BrokerTest.imOnLineState.value = IMOnlineState.Online()
            PurpleLogger.current.d(
                TAG,
                "basicConnect, final connection connected!"
            )
        }.onFailure {
            PurpleLogger.current.d(
                TAG,
                "basicConnect, exception! ${it.message}"
            )
        }
    }

    private fun handleUserChannel(userChanged: Boolean, groupsChanged: Boolean) {
        if (userChanged) {
            runCatching {
                fullDeclare()
            }.onFailure {
                PurpleLogger.current.d(
                    TAG,
                    "handleUserChannel, fullDeclare exception! ${it.message}"
                )
            }
        } else if (groupsChanged) {
            runCatching {
                someDeclare()
            }.onFailure {
                PurpleLogger.current.d(
                    TAG,
                    "handleUserChannel, someDeclare exception! ${it.message}"
                )
            }
        }
    }

    private fun checkConnection(): Boolean {
        val autoRecoveringConnection = connection
        if (autoRecoveringConnection == null) {
            PurpleLogger.current.d(TAG, "checkConnection, connection is null, return")
            return false
        }
        if (!autoRecoveringConnection.isOpen) {
            PurpleLogger.current.d(TAG, "checkConnection, connection is not open, return")
            return false
        }
        val channel = channel
        if (channel == null) {
            PurpleLogger.current.d(TAG, "checkConnection, channel is null, return")
            return false
        }
        if (!channel.isOpen) {
            PurpleLogger.current.d(TAG, "checkConnection, channel is not open, return")
            return false
        }
        return true
    }

    private fun someDeclare() {
        if (!checkConnection()) {
            PurpleLogger.current.d(TAG, "someDeclare, connection not ready， return")
            return
        }
        PurpleLogger.current.d(TAG, "someDeclare, start")
        val channel = channel ?: return
        val queueName = this.queueName ?: return
        val routingKey = "${property.routingKeyPrefix}${property.queuePrefix}${uid}"
        val allExchanges: MutableList<Triple<String, String, BuiltinExchangeType>> = mutableListOf()

        RelationsUseCase.getInstance().getGroupIds().forEach {
            val exchange =
                Triple(it, "${property.groupExchangePrefix}$it", BuiltinExchangeType.FANOUT)
            allExchanges.add(exchange)
        }

        allExchanges.forEach { exchange ->
            PurpleLogger.current.d(
                TAG,
                "someDeclare, exchange:${exchange.second}, type:${exchange.third}"
            )
            channel.exchangeDeclare(exchange.second, exchange.third, false, false, null)
            channel.queueBind(queueName, exchange.second, routingKey)
            if (exchange.third == BuiltinExchangeType.FANOUT) {
                channel.exchangeBind(
                    exchange.second,
                    property.groupExchangeParent,
                    "msg.to.group_${exchange.first}"
                )
            }
        }
        PurpleLogger.current.d(TAG, "someDeclare, final")
    }

    private fun fullDeclare() {
        if (!checkConnection()) {
            PurpleLogger.current.d(TAG, "fullDeclare, connection not ready, return")
            return
        }
        PurpleLogger.current.d(TAG, "fullDeclare, start")
        val channel = channel ?: return
        val uid = uid ?: return
        val queueName = "${property.queuePrefix}${uid}_CUUID_${UUID.randomUUID()}"
        this.queueName = queueName
        val routingKey = "${property.routingKeyPrefix}${property.queuePrefix}${uid}"

        channel.queueDeclare(queueName, false, true, false, null)

        channel.exchangeDeclare(property.groupExchangeParent, BuiltinExchangeType.TOPIC, true)

        channel.exchangeDeclare(property.groupSubRootExchange, BuiltinExchangeType.FANOUT, true)


        channel.exchangeBind(property.groupExchangeParent, property.groupSubRootExchange, "")

        val allExchanges: MutableList<Triple<String, String, BuiltinExchangeType>> = mutableListOf()
        allExchanges.add(
            Triple(
                uid,
                "${property.groupExchangePrefix}$uid",
                BuiltinExchangeType.FANOUT
            )
        )
        RelationsUseCase.getInstance().getGroupIds().forEach {
            val exchange =
                Triple(it, "${property.groupExchangePrefix}$it", BuiltinExchangeType.FANOUT)
            allExchanges.add(exchange)
        }

        allExchanges.forEach { exchange ->
            PurpleLogger.current.d(
                TAG,
                "fullDeclare, exchange:${exchange.second}, type:${exchange.third}"
            )
            channel.exchangeDeclare(exchange.second, exchange.third, false, false, null)
            channel.queueBind(queueName, exchange.second, routingKey)
            if (exchange.third == BuiltinExchangeType.FANOUT) {
                channel.exchangeBind(
                    exchange.second,
                    property.groupExchangeParent,
                    "msg.to.group_${exchange.first}"
                )
            }
        }

        channel.basicConsume(queueName, true, this, this)
        PurpleLogger.current.d(TAG, "fullDeclare, final")
    }

    override fun handle(consumerTag: String?) {
        PurpleLogger.current.d(TAG, "message consume cancel, consume tag is:$consumerTag!")
    }

    override fun handle(consumerTag: String?, message: Delivery?) {
        PurpleLogger.current.d(
            TAG,
            "message delivery from rabbitmq!, consume tag is:$consumerTag, message:$message"
        )
        if (message == null) {
            return
        }
        LocalPurpleCoroutineScope.current.launch {
            IMMessageGenerator.generateByReceived(message)?.let { imMessage ->
                ConversationUseCase.getInstance()
                    .onMessage(LocalApplication.current, imMessage, false)
                LocalMessenger.post(MessageBrokerConstants.MESSAGE_KEY_ON_IM_MESSAGE, imMessage)
            }
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun sendMessage(imObj: IMObj, imMessage: IMMessage) {
        if (!checkConnection()) {
            PurpleLogger.current.d(
                TAG,
                "sendMessage, broker connection not ready! return"
            )
            imMessage.updateSending(
                MessageSendInfo(
                    isSent = false,
                    failureReason = "connection not ready!"
                )
            )
            return
        }
        val channel = channel
        if (channel == null) {
            imMessage.updateSending(
                MessageSendInfo(
                    isSent = false,
                    failureReason = "channel not ready!"
                )
            )
            return
        }

        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "sendMessage")
            val properties = AMQP.BasicProperties.Builder().apply {
                timestamp(imMessage.timestamp)
                type(imMessage.messageType)
                deliveryMode(brokerDeliveryMode)

                val headers = mutableMapOf<String, Any?>()
                headers[IMMessage.HEADER_MESSAGE_MESSAGE_DELIVERY_TYPE] =
                    AppSetsModuleSettings.get().imMessageDeliveryType
                headers[IMMessage.HEADER_MESSAGE_ID] = imMessage.id
                headers[IMMessage.HEADER_MESSAGE_UID] = imMessage.fromInfo.uid
                headers[IMMessage.HEADER_MESSAGE_NAME] = imMessage.fromInfo.bioName
                headers[IMMessage.HEADER_MESSAGE_NAME_BASE64] =
                    imMessage.fromInfo.bioName?.let {
                        Base64.encode(it.toByteArray())
                    }
                headers[IMMessage.HEADER_MESSAGE_AVATAR_URL] =
                    imMessage.fromInfo.avatarUrl
                headers[IMMessage.HEADER_MESSAGE_ROLES] = imMessage.fromInfo.roles

                if (!imMessage.messageGroupTag.isNullOrEmpty()) {
                    headers[IMMessage.HEADER_MESSAGE_MESSAGE_GROUP_TAG] =
                        imMessage.messageGroupTag
                }
                if (imMessage.toInfo.bioId.isNotEmpty()) {
                    headers[IMMessage.HEADER_MESSAGE_TO_ID] = imMessage.toInfo.bioId
                }
                if (!imMessage.toInfo.bioName.isNullOrEmpty()) {
                    headers[IMMessage.HEADER_MESSAGE_TO_NAME] = imMessage.toInfo.bioName
                    headers[IMMessage.HEADER_MESSAGE_TO_NAME_BASE64] =
                        imMessage.toInfo.bioName?.let {
                            Base64.encode(it.toByteArray())
                        }
                }
                if (imMessage.toInfo.toType.isNotEmpty()) {
                    headers[IMMessage.HEADER_MESSAGE_TO_TYPE] = imMessage.toInfo.toType
                }
                if (!imMessage.toInfo.iconUrl.isNullOrEmpty()) {
                    headers[IMMessage.HEADER_MESSAGE_TO_ICON_URL] =
                        imMessage.toInfo.iconUrl
                }
                if (!imMessage.toInfo.roles.isNullOrEmpty()) {
                    headers[IMMessage.HEADER_MESSAGE_TO_ROLES] = imMessage.toInfo.roles
                }

                headers(headers)
            }.build()
            val exchange = property.groupSubRootExchange
            val routingKey = "msg.to.group_${imObj.id}"
            val contentBytes =
                IMMessageGenerator.makeMessageMetadataAsJsonString(imMessage).toByteArray()
            PurpleLogger.current.d(
                TAG,
                "sendMessage, exchange:$exchange, routingKey:$routingKey"
            )
            runCatching {
                channel.basicPublish(exchange, routingKey, properties, contentBytes)
                imMessage.updateSending(MessageSendInfo(isSent = true))
            }.onFailure {
                PurpleLogger.current.e(TAG, "sendMessage, failed, ${it.message}")
                imMessage.updateSending(
                    MessageSendInfo(
                        isSent = false,
                        failureReason = it.localizedMessage
                    )
                )
            }
        }
    }
}
