package xcj.app.appsets.im

import android.util.Log
import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import com.rabbitmq.client.LongString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.ktx.post
import xcj.app.appsets.ui.compose.settings.AppSettings
import xcj.app.io.components.SimpleFileIO
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.Executors

object RabbitMqBroker : MessageBroker<RabbitMqBrokerConfig> {

    val gson: Gson by lazy { Gson() }

    const val ON_MESSAGE_RECEIVE_FROM_RABBIT = "ON_MESSAGE_RECEIVE_FROM_RABBIT"

    val ON_MESSAGE_KEY = ON_MESSAGE_RECEIVE_FROM_RABBIT

    private lateinit var rabbitDaemon: RabbitDaemonThread
    override fun retry() {

    }

    override fun close() {
        if (!RabbitMqBroker::rabbitDaemon.isInitialized)
            return
        rabbitDaemon.close()
    }

    override fun bootstrap(messageBrokerConfig: RabbitMqBrokerConfig) {
        val rabbitProperty = messageBrokerConfig.getRabbitProperty()
        if (!::rabbitDaemon.isInitialized) {
            rabbitDaemon = RabbitDaemonThread()
        }
        rabbitDaemon.updateUserChannel(rabbitProperty)
    }

    override suspend fun sendMessage(imObj: ImObj, imMessage: ImMessage) {
        rabbitDaemon.sendMessage(imObj, imMessage)
    }

    private class RabbitDaemonThread : DeliverCallback, Runnable {
        private val TAG = "RabbitDaemonThread"
        private lateinit var connection: Connection
        private lateinit var channel: Channel
        private lateinit var rabbitmqProperty: RabbitMqBrokerProperty
        private var userChanged: Boolean = true
        private var userGroupsChanged: Boolean = true
        private lateinit var factory: ConnectionFactory
        private var queueName: String? = null
        private var routingKey: String? = null
        private val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)

        /**
         * start point
         */
        fun updateUserChannel(outRabbitmqProperty: RabbitMqBrokerProperty) {
            if (!::factory.isInitialized) {
                factory = ConnectionFactory()
            }
            factory.apply {
                host = outRabbitmqProperty.`rabbit-host`
                port = outRabbitmqProperty.`rabbit-port`
                Log.e("RabbitDaemonThread", "rabbit host:port:${host}:${port}")
                username = outRabbitmqProperty.`rabbit-admin-username`
                password = outRabbitmqProperty.`rabbit-admin-password`
                virtualHost = outRabbitmqProperty.`rabbit-virtual-host`
            }
            if (::rabbitmqProperty.isInitialized) {
                userChanged = rabbitmqProperty.uid != outRabbitmqProperty.uid
                userGroupsChanged =
                    rabbitmqProperty.`user-exchange-groups` != outRabbitmqProperty.`user-exchange-groups`
            } else {
                userChanged = true
                userGroupsChanged = true
            }
            rabbitmqProperty = outRabbitmqProperty
            Log.e(TAG, "updateUserChannel")
            executeSelf()
        }

        fun deleteUserQueue() {
            if (!::connection.isInitialized)
                return
            if (!connection.isOpen)
                return
            if (!::channel.isInitialized)
                return
            if (!channel.isOpen)
                return
            if (queueName.isNullOrEmpty())
                return
            channel.queueDelete(queueName)
        }

        fun close() {
            deleteUserQueue()
            if (::channel.isInitialized)
                if (channel.isOpen)
                    channel.close()
            if (::connection.isInitialized)
                if (connection.isOpen)
                    connection.close()
        }

        fun basicConnect() {
            if (::connection.isInitialized && connection.isOpen && !userChanged)
                return
            connection = try {
                factory.newConnection() ?: return
            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
                Log.e(
                    TAG,
                    "rabbitmq SocketTimeoutException! thread id:${Thread.currentThread().id}"
                )
                return
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(
                    TAG,
                    "rabbitmq IOException! thread id:${Thread.currentThread().id}"
                )
                return
            }
            if (!connection.isOpen)
                return
            Log.i(
                TAG,
                "rabbitmq connected! thread id:${Thread.currentThread().id}\""
            )
            channel = try {
                connection.createChannel()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(
                    TAG,
                    "rabbitmq create channel failed! thread id:${Thread.currentThread().id}\""
                )
                return
            }
        }

        override fun run() {
            basicConnect()
            try {
                doUserChannelThings()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(
                    TAG,
                    "rabbitmq exchange bind or queue bind or create exception! thread id:${Thread.currentThread().id}\""
                )
            }
        }


        private fun doUserChannelThings() {
            if (userChanged) {
                fullDeclare()
                return
            }
            if (userGroupsChanged) {
                someDeclare()
            }
        }

        fun someDeclare() {
            if (!::connection.isInitialized || !::channel.isInitialized)
                return
            if (!connection.isOpen || !channel.isOpen)
                return
            val groupsExchanges: MutableList<Pair<String, BuiltinExchangeType>> = mutableListOf()
            val groupExchangePrefix = rabbitmqProperty.`user-exchange-group-prefix`
            if (!rabbitmqProperty.`user-exchange-groups`.isNullOrEmpty()) {
                val groupExchangeNames: String = rabbitmqProperty.`user-exchange-groups`!!
                groupExchangeNames.split(",").forEach {
                    val groupExchange = "$groupExchangePrefix$it" to BuiltinExchangeType.FANOUT
                    groupsExchanges.add(groupExchange)
                }
                groupsExchanges.forEach { exchange ->
                    //定义单聊交换机以及群组交换机
                    channel.exchangeDeclare(exchange.first, exchange.second, true, false, null)
                    //把自己的队列绑定到单聊交换机和群组交换机上
                    channel.queueBind(queueName, exchange.first, routingKey)
                }
            }
        }

        fun fullDeclare() {
            if (!::connection.isInitialized || !::channel.isInitialized)
                return
            if (!connection.isOpen || !channel.isOpen)
                return
            queueName =
                rabbitmqProperty.`queue-prefix` + rabbitmqProperty.uid + "_CUUID_" + UUID.randomUUID()
                    .toString()
            routingKey =
                rabbitmqProperty.`routing-key-prefix` + rabbitmqProperty.`queue-prefix` + rabbitmqProperty.uid
            val userTopicExchangeName = rabbitmqProperty.`user-exchange-main`
            val userTopicExchange = userTopicExchangeName to BuiltinExchangeType.TOPIC

            val allExchanges: MutableList<Pair<String, BuiltinExchangeType>> =
                mutableListOf()
            allExchanges.add(userTopicExchange)
            val groupExchangePrefix = rabbitmqProperty.`user-exchange-group-prefix`
            if (!rabbitmqProperty.`user-exchange-groups`.isNullOrEmpty()) {
                val groupExchangeNames: String = rabbitmqProperty.`user-exchange-groups`!!
                groupExchangeNames.split(",").forEach {
                    val groupExchange = "$groupExchangePrefix$it" to BuiltinExchangeType.FANOUT
                    allExchanges.add(groupExchange)
                }
            }
            channel.run {
                //定义自己的队列
                //多设备需要为每个设备定义不同的队列
                queueDeclare(queueName, false, true, false, null)
                allExchanges.forEach { exchange ->
                    //定义单聊交换机以及群组交换机
                    exchangeDeclare(exchange.first, exchange.second, true, false, null)
                    //把自己的队列绑定到单聊交换机和群组交换机上
                    queueBind(queueName, exchange.first, routingKey)
                }

                val userFanExchangeName = rabbitmqProperty.`user-exchange-main-parent`
                //定义单聊用户的父交换机，fan out类型
                exchangeDeclare(userFanExchangeName, BuiltinExchangeType.FANOUT, true, false, null)
                //将单聊交换机绑定到fan out类型的父交换机上
                exchangeBind(userTopicExchangeName, userFanExchangeName, routingKey)

            }
            channel.basicConsume(queueName, true, this, CancelCallback { })
        }


        override fun handle(consumerTag: String?, message: Delivery?) {
            if (message == null)
                return
            Log.e(TAG, "message delivery from rabbitmq!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
            val headers = message.properties.headers
            val imMessageType = message.properties.type
            val imMessageTimestamp = message.properties.timestamp
            val fromUid = if (headers.containsKey("uid")) {
                (headers["uid"] as? LongString).toString()
            } else null
            if (fromUid.isNullOrEmpty())
                return
            val msgId = if (headers.containsKey("msgId")) {
                (headers["msgId"] as? LongString).toString()
            } else return
            val fromName = if (headers.containsKey("name")) {
                (headers["name"] as? LongString).toString()
            } else null
            val fromAvatarUrl = if (headers.containsKey("avatarUrl")) {
                (headers["avatarUrl"] as? LongString).toString()
            } else null
            val fromRoles = if (headers.containsKey("roles")) {
                (headers["roles"] as? LongString).toString()
            } else null
            val toRoles = if (headers.containsKey("toRoles")) {
                (headers["toRoles"] as? LongString).toString()
            } else null
            val toType = if (headers.containsKey("toType")) {
                (headers["toType"] as? LongString).toString()
            } else return
            val toId = if (headers.containsKey("toId")) {
                (headers["toId"] as? LongString).toString()
            } else return
            val toName = if (headers.containsKey("toName")) {
                (headers["toName"] as? LongString).toString()
            } else return
            val toIconUrl = if (headers.containsKey("toIconUrl")) {
                (headers["toIconUrl"] as? LongString).toString()
            } else null
            val groupMessageTag = if (headers.containsKey("groupMessageTag")) {
                (headers["groupMessageTag"] as? LongString).toString()
            } else null
            val imUserFromInfo = MessageFromInfo(fromUid, fromName, fromAvatarUrl, fromRoles)
            val imToInfo = MessageToInfo(toType, toId, toName, toIconUrl, toRoles)
            val imContent = String(message.body)
            val imMessage: ImMessage? = when (imMessageType) {
                RabbitMqBrokerPropertyDesignType.TYPE_TEXT ->
                    ImMessage.Text(
                        msgId,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )

                RabbitMqBrokerPropertyDesignType.TYPE_IMAGE ->
                    ImMessage.Image(
                        msgId,
                        transformToUrlIfNeeded(imContent),
                        null,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )

                RabbitMqBrokerPropertyDesignType.TYPE_VOICE -> {
                    val voiceJson = gson.fromJson(imContent, CommonURLJson.VoiceURLJson::class.java)
                    voiceJson.url = transformToUrlIfNeeded(voiceJson.url)
                    ImMessage.Voice(
                        msgId,
                        voiceJson,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )
                }


                RabbitMqBrokerPropertyDesignType.TYPE_LOCATION ->
                    ImMessage.Location(
                        msgId,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )

                RabbitMqBrokerPropertyDesignType.TYPE_VIDEO -> {
                    val videoJson = gson.fromJson(imContent, CommonURLJson.VideoURLJson::class.java)
                    videoJson.url = transformToUrlIfNeeded(videoJson.url)
                    ImMessage.Video(
                        msgId,
                        videoJson,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_AD ->
                    ImMessage.Ad(
                        msgId,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )

                RabbitMqBrokerPropertyDesignType.TYPE_MUSIC -> {
                    val musicJson = gson.fromJson(imContent, CommonURLJson.MusicURLJson::class.java)
                    musicJson.url = transformToUrlIfNeeded(musicJson.url)
                    ImMessage.Music(
                        msgId,
                        musicJson,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_FILE -> {
                    val fileJson = gson.fromJson(imContent, CommonURLJson.FileURLJson::class.java)
                    fileJson.url = transformToUrlIfNeeded(fileJson.url)
                    ImMessage.File(
                        msgId,
                        fileJson,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        message.properties.contentType,
                        groupMessageTag
                    )
                }

                RabbitMqBrokerPropertyDesignType.TYPE_HTML ->
                    ImMessage.HTML(
                        msgId,
                        imContent,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        groupMessageTag
                    )

                RabbitMqBrokerPropertyDesignType.TYPE_SYSTEM -> {
                    val systemContentJson =
                        gson.fromJson(imContent, SystemContentJson::class.java)
                    when (systemContentJson.type) {
                        "add_friend_request" -> {
                            systemContentJson.contentObject =
                                gson.fromJson(
                                    systemContentJson.content,
                                    SystemContentInterface.FriendRequestJson::class.java
                                )
                        }

                        "add_friend_request_feedback" -> {
                            systemContentJson.contentObject =
                                gson.fromJson(
                                    systemContentJson.content,
                                    SystemContentInterface.FriendRequestFeedbackJson::class.java
                                )
                        }

                        "join_group_request" -> {
                            systemContentJson.contentObject =
                                gson.fromJson(
                                    systemContentJson.content,
                                    SystemContentInterface.GroupRequestJson::class.java
                                )
                        }

                        "join_group_request_feedback" -> {
                            systemContentJson.contentObject =
                                gson.fromJson(
                                    systemContentJson.content,
                                    SystemContentInterface.GroupJoinRequestFeedbackJson::class.java
                                )
                        }
                    }
                    ImMessage.System(
                        msgId,
                        systemContentJson,
                        imUserFromInfo,
                        imMessageTimestamp,
                        imToInfo,
                        null,
                        groupMessageTag,
                    )
                }

                else -> null
            }
            if (imMessage != null) {
                val time = sdf.format(imMessage.date)
                imMessage.dateStr = time
                ON_MESSAGE_KEY.post(imMessage)
            }
        }

        private fun transformToUrlIfNeeded(string: String): String {
            if (string.isHttpUrl())
                return string
            return SimpleFileIO.getInstance().generatePreSign(string) ?: string
        }

        fun isConnect(): Boolean {
            if (!::connection.isInitialized)
                return false
            return connection.isOpen
        }

        fun executeSelf() {
            Executors.newSingleThreadExecutor().execute(this)
        }

        //val emptyProperties: AMQP.BasicProperties = AMQP.BasicProperties.Builder().build()

        suspend fun sendMessage(imObj: ImObj, imMessage: ImMessage) {
            if (!isConnect())
                return
            if (imObj is ImObj.ImTitle)
                return
            withContext(Dispatchers.IO) {
                runCatching {
                    val properties = AMQP.BasicProperties.Builder().apply {
                        timestamp(imMessage.date)
                        type(RabbitMqBrokerPropertyDesignType.getTypeByImMessage(imMessage))
                        contentType(imMessage.contentType)
                        val headers = mutableMapOf<String, Any?>()
                        headers["messageDeliveryType"] = AppSettings.messageDeliveryType
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
                        headers(headers)
                    }.build()
                    val exchange = when (imObj) {
                        is ImObj.ImSingle -> rabbitmqProperty.`user-exchange-main-parent`
                        is ImObj.ImGroup -> "${rabbitmqProperty.`user-exchange-group-prefix`}${imObj.groupId}"
                        else -> return@runCatching
                    }
                    var routingKey = when (imObj) {
                        is ImObj.ImSingle -> "${rabbitmqProperty.`routing-key-prefix`}${rabbitmqProperty.`queue-prefix`}${imObj.uid}"
                        is ImObj.ImGroup -> "msg.to.group_${imObj.groupId}}"
                        else -> return@runCatching
                    }
                    //给其他人发送
                    val contentBytes = imMessage.content.toByteArray()
                    channel.basicPublish(
                        exchange,
                        routingKey,
                        properties,
                        contentBytes
                    )
                    if (imObj is ImObj.ImSingle) {
                        //TODO, 第二种解决方法：再加一个queue,routingKey为另一个，即可避免发送的时候发给当前的自己
                        routingKey =
                            "${rabbitmqProperty.`routing-key-prefix`}${rabbitmqProperty.`queue-prefix`}${rabbitmqProperty.uid}"
                        //给自己的设备发送
                        channel.basicPublish(
                            exchange,
                            routingKey,
                            properties,
                            contentBytes
                        )
                    }
                }
            }
        }
    }
}
