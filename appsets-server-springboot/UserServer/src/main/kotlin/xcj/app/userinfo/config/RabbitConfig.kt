package xcj.app.userinfo.config

import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import xcj.app.userinfo.im.RabbitMqBroker
import xcj.app.userinfo.model.table.mysql.appSetsUserAdmin0
import java.util.*

@Configuration
class RabbitConfig {
    @Bean
    fun rabbitMessageBroker(ampqAdmin: AmqpAdmin, rabbitMessagingTemplate: RabbitMessagingTemplate): RabbitMqBroker {
        val appSetsUserAdmin0 = appSetsUserAdmin0()
        val queueName = "user_"+appSetsUserAdmin0.uid+"_CUUID_" + UUID.randomUUID()
        val queue = Queue(queueName, false, true, false, null)
        ampqAdmin.declareQueue(queue)
        println("rabbitMessageBroker")
        return RabbitMqBroker(rabbitMessagingTemplate)
    }
}