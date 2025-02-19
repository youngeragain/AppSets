package xcj.app.appsets.im

class RabbitMQBrokerConfig(
    private val mRabbitMQBrokerProperty: RabbitMQBrokerProperty
) : MessageBrokerConfig {

    fun getRabbitProperty(): RabbitMQBrokerProperty {
        return mRabbitMQBrokerProperty
    }

}