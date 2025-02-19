package xcj.app.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class Duck:BeanPostProcessor {
    val log: Logger = LoggerFactory.getLogger(Duck::class.java)
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        log.debug("bean: $bean")
        return super.postProcessBeforeInitialization(bean, beanName)
    }
}