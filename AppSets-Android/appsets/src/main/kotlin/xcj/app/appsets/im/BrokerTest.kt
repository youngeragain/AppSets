package xcj.app.appsets.im

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.JsonObject
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.settings.ModuleConfig
import xcj.app.starter.android.util.PurpleLogger
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object BrokerTest {

    private const val TAG = "BrokerTest"

    private val broker = RabbitMQBroker()

    val imOnLineState: MutableState<IMOnlineState> = mutableStateOf(IMOnlineState.Offline)

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun start() {
        PurpleLogger.current.d(TAG, "start")
        if (!LocalAccountManager.isLogged()) {
            PurpleLogger.current.d(
                TAG,
                "start, failed! because of use not login! return"
            )
            return
        }

        val appConfig = ModuleConfig.moduleConfiguration
        if (appConfig.imBrokerProperties.isEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "start, failed!" +
                        " because of imBrokerProperties isNullOrEmpty, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "start, AppConfig.appConfiguration.imBrokerProperties:${appConfig.imBrokerProperties}"
        )

        val decodeConfig =
            Base64.decode(appConfig.imBrokerProperties.toByteArray())
                .decodeToString()
        PurpleLogger.current.d(
            TAG,
            "start, decodeConfig:$decodeConfig"
        )
        if (decodeConfig.isEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "start, failed!" +
                        " decode RabbitProperties get null, return"
            )
            return
        }
        runCatching {
            val jsonObject = Gson().fromJson<JsonObject>(
                decodeConfig,
                JsonObject::class.java
            )
            PurpleLogger.current.d(
                TAG,
                "start, configMap:${jsonObject}"
            )
            val rabbitMqBrokerConfig = RabbitMQBrokerConfig(
                RabbitMQBrokerProperty(
                    host = jsonObject["rabbit-host"].asString,
                    port = jsonObject["rabbit-port"].asInt,
                    username = jsonObject["rabbit-admin-username"].asString,
                    password = jsonObject["rabbit-admin-password"].asString,
                    virtualHost = jsonObject["rabbit-virtual-host"].asString,
                    queuePrefix = jsonObject["queue-prefix"].asString,
                    routingKeyPrefix = jsonObject["routing-key-prefix"].asString,
                    groupExchangePrefix = "one2many-fanout-",
                    groupExchangeParent = "one2many-topic",
                    groupRootExchange = "one2many-fanout-root",
                    groupSubRootExchange = "one2many-fanout-subroot"
                )
            )
            rabbitMqBrokerConfig
        }.onSuccess { config ->
            broker.bootstrap(config)
        }.onFailure {
            PurpleLogger.current.d(
                TAG,
                "start, failed!" +
                        " RabbitProperties map get failed, ${it.message}\n ${it.stackTraceToString()}"
            )
        }
    }

    suspend fun sendMessage(imObj: IMObj, imMessage: IMMessage) {
        PurpleLogger.current.d(TAG, "sendMessage")
        broker.sendMessage(imObj, imMessage)
    }

    suspend fun close() {
        broker.close()
    }

    suspend fun updateImGroupBindIfNeeded() {
        broker.updateImGroupBindIfNeeded()
    }

}