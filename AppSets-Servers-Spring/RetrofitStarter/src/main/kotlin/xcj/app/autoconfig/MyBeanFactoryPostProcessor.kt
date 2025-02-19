package xcj.app.autoconfig

import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.stereotype.Component
import xcj.app.singleton.ApplicationProvider


@Component
class MyBeanFactoryPostProcessor:BeanFactoryPostProcessor {

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        if(beanFactory is DefaultListableBeanFactory){
            ApplicationProvider.beanFactory = beanFactory
            //beanFactory.registerSingleton()
        }
    }

}