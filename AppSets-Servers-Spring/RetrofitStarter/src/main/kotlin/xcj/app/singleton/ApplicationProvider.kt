package xcj.app.singleton

import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.SpringApplication
import org.springframework.cloud.client.ServiceInstance
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext

object ApplicationProvider {
    lateinit var applicationContext: ApplicationContext
    lateinit var configurableApplicationContext: ConfigurableApplicationContext
    lateinit var springApplication: SpringApplication
    lateinit var beanFactory: DefaultListableBeanFactory
    val cmdArgs:MutableList<String> by lazy { mutableListOf() }
    val servers:MutableList<ServiceInstance> by lazy { mutableListOf() }
}