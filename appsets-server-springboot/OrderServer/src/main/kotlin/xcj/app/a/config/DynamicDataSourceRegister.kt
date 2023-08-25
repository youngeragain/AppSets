package xcj.app.a.config

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertyName
import org.springframework.boot.context.properties.source.ConfigurationPropertyNameAliases
import org.springframework.boot.context.properties.source.ConfigurationPropertySource
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import jakarta.sql.DataSource


/**
 * 参考了 https://www.cnblogs.com/shihaiming/p/11067623.html
 */
class DynamicDataSourceRegister:ImportBeanDefinitionRegistrar, EnvironmentAware{
    private val log:Logger = LoggerFactory.getLogger(DynamicDataSourceRegister::class.java)
    private lateinit var mEnvironment: Environment
    private lateinit var binder:Binder
    private val customDataSources: MutableMap<Int, MyDataSourceWrapper?> = mutableMapOf()
    private var defaultDataSourceId:Int? = null
    private val aliases = ConfigurationPropertyNameAliases().apply {
        addAliases("url", "jdbc-url")
        addAliases("username", "user")
        addAliases("password", "pw", "psw")
    }
    /**
     * 线程级别的私有变量
     * 查询前设置，查询后清除
     */
    private val currentThreadDataSourceId = ThreadLocal<Int>()
    init {
        instance = this
    }
    fun containsDataSource(id: Int?):Boolean {
        id?:return false
        return customDataSources.containsKey(id)
    }
    private fun getDefaultDataSourceId():Int{
        return if(defaultDataSourceId==null) {
            val id = customDataSources.values.first { it?.isDefault == true }?.id
            id?:throw RuntimeException("No default dataSource id")
            defaultDataSourceId = id
            id
        } else
            defaultDataSourceId!!

    }
    override fun setEnvironment(environment: Environment) {
        mEnvironment = environment
        binder = Binder.get(environment)
    }
//    @ConfigurationProperties(prefix = "spring.datasource.custom")
    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val configs: List<Map<*, *>> = binder.bind(
            "spring.datasource.custom",
            Bindable.listOf(Map::class.java)).get()
        // 遍历从数据源
        var config:Any
        var id:Int
        for (i in configs.indices) {
            config = configs[i]
            id = config["id"] as? Int ?: continue
            // 获取数据源的key，以便通过该key可以定位到数据源
            customDataSources[id] = MyDataSourceWrapper(
                bind(getDataSourceType(config["type"] as? String), config),
                id,
                (config["default"] as? Boolean)?:false)
            // 数据源上下文，用于管理数据源与记录已经注册的数据源key
        }
        val defaultDatasource:DataSource? = when(customDataSources.size){
            0-> return
            1->{
                customDataSources[0]?.apply {
                    isDefault = true
                }?.dataSource
            }
            else->{
                var customDataSource:DataSource? = null
                var defaultCount = 0
                var index = 0
                customDataSources.forEach { (key, myDataSourceWrapper) ->
                    index++
                    if(myDataSourceWrapper?.isDefault == true) {
                        defaultCount++
                        if(customDataSource==null)
                            customDataSource = myDataSourceWrapper.dataSource
                        else
                            myDataSourceWrapper.isDefault = false
                    }else if(defaultCount==0&&index==customDataSources.size-1){
                        val myDataSourceWrapper1 = customDataSources[0]
                        myDataSourceWrapper1?.isDefault = true
                        customDataSource = myDataSourceWrapper1?.dataSource
                    }
                }
                if(defaultCount>1){
                    log.debug("More than one datasource set to default! use the first default datasource")
                }else if(defaultCount==0){
                    log.debug("No datasource set to be default! use the first as the default datasource")
                }
                customDataSource
            }
        }
        // bean定义类
        val define = GenericBeanDefinition().apply {
            setBeanClass(DynamicDataSource::class.java)
            val mpv = propertyValues
            // 添加默认数据源，避免key不存在的情况没有数据源可用
            // 需要注入的参数
            mpv.add("defaultTargetDataSource", defaultDatasource)
            // 添加其他数据源
            val sources =  customDataSources.filter {
                it.value?.isDefault==false
            }.mapValues {
                it.value?.dataSource
            }
            if(sources.isNotEmpty())
                mpv.add("targetDataSources", sources)
        }
        // 将该bean注册为datasource，不使用springboot自动生成的datasource
        registry.registerBeanDefinition("dataSource", define)
    }

    private fun getDataSourceType(typeStr: String?): Class<out DataSource?>? {
        return try {
            if (!typeStr.isNullOrEmpty()) {
                // 字符串不为空则通过反射获取class对象
                Class.forName(typeStr) as Class<out DataSource?>
            } else {
                // 默认为hikariCP数据源，与springboot默认数据源保持一致
                HikariDataSource::class.java
            }
        } catch (e: Exception) {
            //无法通过反射获取class对象的情况则抛出异常，该情况一般是写错了，所以此次抛出一个runtimeException
            throw IllegalArgumentException("can not resolve class with type: $typeStr")
        }
    }

    /**
     * 绑定参数，以下三个方法都是参考DataSourceBuilder的bind方法实现的，目的是尽量保证我们自己添加的数据源构造过程与springboot保持一致
     *
     * @param result
     * @param properties
     */
    private fun bind(result: DataSource, properties: Map<*, *>) {
        val source: ConfigurationPropertySource = MapConfigurationPropertySource(properties)
        val binder = Binder(source.withAliases(aliases))
        // 将参数绑定到对象
        binder.bind(ConfigurationPropertyName.EMPTY, Bindable.ofInstance(result))
    }

    private fun <T : DataSource?> bind(clazz: Class<T>?, properties: Map<*, *>): T {
        val source: ConfigurationPropertySource = MapConfigurationPropertySource(properties)
        val binder = Binder(source.withAliases(aliases))
        // 通过类型绑定参数并获得实例对象
        return binder.bind(ConfigurationPropertyName.EMPTY, Bindable.of(clazz)).get()
    }

    /**
     * @param clazz
     * @param sourcePath 参数路径，对应配置文件中的值，如: spring.datasource
     * @param <T>
     * @return
    </T> */
    private fun <T : DataSource?> bind(clazz: Class<T>, sourcePath: String): T {
        val properties: Map<*, *> = binder.bind(sourcePath, Map::class.java).get()
        return bind(clazz, properties)
    }


    fun getDataSourceRouterKey(): Int? {
        return currentThreadDataSourceId.get()
    }

    fun setDefaultDataSourceKey(){
        currentThreadDataSourceId.set(getDefaultDataSourceId())
    }
    fun setDataSourceRouterKey(dataSourceRouterId: Int) {
        currentThreadDataSourceId.set(dataSourceRouterId)
    }

    /**
     * 设置数据源之前一定要先移除
     */
    fun removeDataSourceRouterKey() {
        currentThreadDataSourceId.remove()
    }

    companion object{
        lateinit var instance:DynamicDataSourceRegister
    }
}