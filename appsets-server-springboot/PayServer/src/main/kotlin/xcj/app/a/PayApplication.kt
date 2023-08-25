package xcj.app.a

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.messaging.support.GenericMessage
import java.net.http.WebSocket

@SpringBootApplication
class PayApplication {
    @Autowired
    lateinit var rabbitTemplate: RabbitMessagingTemplate
    lateinit var rabbitAdmin: RabbitAdmin
/*    @Bean
    fun runner():ApplicationRunner{
        return ApplicationRunner{
*//*                val fanoutExchange = FanoutExchange("one2one-fanout")
            rabbitAdmin.declareExchange(fanoutExchange)
            val queue = Queue("im-sever-queue-for-saving")
            rabbitAdmin.declareQueue(queue)
            rabbitAdmin.declareBinding(BindingBuilder.bind(queue).to(fanoutExchange))

            *//**//*while (true){
                rabbitTemplate.send("fanout-exchange-for-user-group1", "",
                    GenericMessage<String>("send msg in ${System.currentTimeMillis()}")
                )
                Thread.sleep(3000)
            }*//*
            rabbitTemplate.send("one2one-fanout", "",
                GenericMessage<String>("send msg in ${System.currentTimeMillis()}")
            )
        }
    }*/
    @RabbitListener(queues = ["im-sever-queue-for-saving"])
    fun onMessage(msg:String){
        println("im-sever-queue-for-saving==${System.currentTimeMillis()} received msg:$msg")
    }
   /* @RabbitListener(queues = ["queueTom"])
    fun onMessage1(msg:String){
        println("queueTom==${System.currentTimeMillis()} received msg:$msg")
    }*/
}

fun main() {
    MySpringApplication(PayApplication::class.java).run()
}

class MySpringApplication<T>(vararg sources:Class<T>):SpringApplication(*sources){

}