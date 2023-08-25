package xcj.app.core.test

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import xcj.app.core.foundation.Aware
import xcj.app.core.foundation.PurpleRawScope
import kotlin.coroutines.EmptyCoroutineContext

abstract class PurpleContext : PurpleRawScope {

    override val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.Main + EmptyCoroutineContext + CoroutineExceptionHandler { coroutineContext, throwable -> })

    val definitionClassMap: MutableMap<Class<*>, MutableList<Class<*>>> = mutableMapOf()
    val definitionInstanceList: MutableList<Any> = mutableListOf()
    val definitionAwareInstanceList: MutableList<Aware> = mutableListOf()
    val definitionContextListenerInstanceList: MutableList<PurpleContextListener> = mutableListOf()

    //key 实例对象, value 对象的beanDefinition
    val definitionInstanceMap: MutableMap<Any, PurpleBeanDefinition> = mutableMapOf()

    fun <T> getAwareList(clazz: Class<T>): List<T> {
        return definitionAwareInstanceList.filterIsInstance(clazz)
    }

    fun getPurpleContextListenerList(): List<PurpleContextListener> {
        return definitionContextListenerInstanceList
    }
}

