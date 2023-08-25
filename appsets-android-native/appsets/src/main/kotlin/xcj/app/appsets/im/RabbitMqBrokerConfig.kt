package xcj.app.appsets.im

class RabbitMqBrokerConfig(private val mRabbitMqBrokerProperty: RabbitMqBrokerProperty) :
    MessageBrokerConfig {
    fun getRabbitProperty(): RabbitMqBrokerProperty {
        return mRabbitMqBrokerProperty
    }
}