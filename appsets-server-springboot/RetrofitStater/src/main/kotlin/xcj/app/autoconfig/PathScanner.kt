package xcj.app.autoconfig

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.core.type.filter.AnnotationTypeFilter
import xcj.app.annotation.ApplicationName

class PathScanner(registry: BeanDefinitionRegistry): ClassPathBeanDefinitionScanner(registry, false){
    init {
        resetFilters(false)
        addIncludeFilter(AnnotationTypeFilter(ApplicationName::class.java, true, true))
    }

    override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        val hasAnnotation = beanDefinition.metadata.hasAnnotation(ApplicationName::class.java.name)
        if(hasAnnotation){
            (beanDefinition as? GenericBeanDefinition)?.apply {
                isAbstract = true
                autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE
                scope = BeanDefinition.SCOPE_SINGLETON
                try {
                    beanClass
                }catch (e:Exception){
                    e.printStackTrace()
                    try {
                        setBeanClass(Class.forName(beanClassName))
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
        }
        return hasAnnotation
    }

    override fun checkCandidate(beanName: String, beanDefinition: BeanDefinition): Boolean {
        return super.checkCandidate(beanName, beanDefinition)
    }
}