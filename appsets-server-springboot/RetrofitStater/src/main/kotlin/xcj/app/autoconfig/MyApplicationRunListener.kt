package xcj.app.autoconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.ConfigurableBootstrapContext
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.type.filter.AnnotationTypeFilter
import xcj.app.annotation.ApplicationName
import xcj.app.singleton.ApplicationProvider
import xcj.app.topfun.getApi



class MyApplicationContextInitializer: ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val log: Logger = LoggerFactory.getLogger(MyApplicationContextInitializer::class.java)

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        log.debug("initialize")
        ApplicationProvider.configurableApplicationContext = applicationContext


    }


}

class MyApplicationRunListener(springApplication: SpringApplication, args:Array<String>): SpringApplicationRunListener {

    init {
        ApplicationProvider.springApplication = springApplication
        if(ApplicationProvider.cmdArgs.isNotEmpty()){
            ApplicationProvider.cmdArgs.clear()
        }
        ApplicationProvider.cmdArgs.addAll(args)
    }

    override fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext?,
        environment: ConfigurableEnvironment?
    ) {
        super.environmentPrepared(bootstrapContext, environment)
    }

}