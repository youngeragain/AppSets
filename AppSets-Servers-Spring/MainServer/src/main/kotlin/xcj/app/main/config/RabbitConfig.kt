package xcj.app.main.config

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xcj.app.main.im.RabbitMQBroker
import xcj.app.main.im.RabbitMQBrokerProperty

@Configuration
class RabbitConfig {

    companion object {
        private const val TAG = "RabbitConfig"
    }

    @Bean
    fun rabbitMessageBroker(ampqAdmin: AmqpAdmin, rabbitMessagingTemplate: RabbitMessagingTemplate): RabbitMQBroker {
        val property = RabbitMQBrokerProperty(
            host = "localhost",
            port = 5672,
            username = "",
            password = "",
            virtualHost = "/",
            queuePrefix = "user_",
            routingKeyPrefix = "msg.",
            groupExchangePrefix = "one2many-fanout-",
            groupExchangeParent = "one2many-topic",
            groupRootExchange = "one2many-fanout-root",
            groupSubRootExchange = "one2many-fanout-subroot"
        )
        return RabbitMQBroker(ampqAdmin, rabbitMessagingTemplate, property)
    }
}