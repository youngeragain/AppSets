package xcj.app.autoconfig

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ConfigurableBootstrapContext
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import xcj.app.singleton.ApplicationProvider


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