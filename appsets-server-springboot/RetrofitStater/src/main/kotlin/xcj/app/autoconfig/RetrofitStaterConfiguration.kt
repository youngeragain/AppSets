package xcj.app.autoconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.cloud.zookeeper.ConditionalOnZookeeperEnabled
import org.springframework.cloud.zookeeper.discovery.ZookeeperDiscoveryClient
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Configuration
import xcj.app.annotation.ApplicationName
import xcj.app.singleton.ApplicationProvider
import xcj.app.topfun.getApi

@Configuration
@ConditionalOnZookeeperEnabled
@Qualifier("customRetrofitConfiguration")
class RetrofitStaterConfiguration :ApplicationContextAware, InitializingBean {
    private val log: Logger = LoggerFactory.getLogger(RetrofitStaterConfiguration::class.java)
    private lateinit var pathScanner:PathScanner
    @Autowired
    lateinit var zookeeperDiscoveryClient: ZookeeperDiscoveryClient

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        ApplicationProvider.applicationContext = applicationContext
    }

    override fun afterPropertiesSet() {
        /*zookeeperDiscoveryClient.services.forEach {
            ApplicationProvider.servers.addAll(zookeeperDiscoveryClient.getInstances(it))
            log.debug("afterPropertiesSet:${ApplicationProvider.servers}")
        }

        val applicationContext1 = ApplicationProvider.configurableApplicationContext
        if(applicationContext1 is BeanDefinitionRegistry){
            pathScanner = PathScanner(applicationContext1)
            CoroutineScope(Dispatchers.IO).launch {
                doRegisterBean(applicationContext1)
            }
        }*/
    }
    private suspend fun doRegisterBean(applicationContext1: BeanDefinitionRegistry) {
        val packages:MutableList<String> = mutableListOf()
        val packageBuilder:StringBuilder =  StringBuilder()
        ApplicationProvider.springApplication.allSources.forEach {
            val split = ((it as? Class<*>)?.canonicalName)?.split(".")
            split?.forEachIndexed{ index, string ->
                packageBuilder.clear()
                if(index==0){
                    packages.add(string)
                }else{
                    for(i in 0..index){
                        packageBuilder.append(split[i])
                        if(i!=index)
                            packageBuilder.append(".")
                    }
                    packages.add(packageBuilder.toString())
                }
            }
        }
        if(packages.isNotEmpty()){
            //use the top of packages
            val elements = pathScanner.findCandidateComponents(packages[0]).map { it as? GenericBeanDefinition }
            elements.forEach {
                val takeWhile =
                    (it as? AnnotatedBeanDefinition)?.metadata?.annotations?.takeWhile { a ->
                        a.type.isAssignableFrom(ApplicationName::class.java)
                    }
                assert(takeWhile?.size==1)
                val b = getApi(it?.beanClass!!)
                val beanFactory = (applicationContext1 as? ConfigurableApplicationContext)?.beanFactory
                if(beanFactory is DefaultListableBeanFactory){
                    beanFactory.registerSingleton("PayApi", b)
                }
            }
        }
    }
}

fun Class<*>.getCurrentPackagePath():String {
    var path = protectionDomain.codeSource.location.path
    if(System.getProperty("os.name").equals("windows", true)){
        path = path.substring(1, path.length)
    }
    if(path.contains("jar")){
        path = path.substring(0, path.lastIndexOf("."))
        return path.substring(0, path.lastIndexOf("/"))
    }
    return path.replace("target/classes/","")
}
