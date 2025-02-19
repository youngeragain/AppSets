package xcj.app.appsets.im

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