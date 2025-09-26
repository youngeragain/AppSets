package xcj.app.starter.test

import android.content.res.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.Aware
import xcj.app.starter.foundation.DesignEvent
import xcj.app.starter.foundation.PurpleLifecycle
import kotlin.coroutines.EmptyCoroutineContext

abstract class PurpleContext :
    PurpleLifecycle,
    PurpleEventPublisher,
    PurpleEventSubscriber,
    ApplicationCallback {
    companion object {
        private const val TAG = "PurpleContext"
    }
    val definitionAnythingComponents: MutableList<String> = mutableListOf()
    val definitionClassMap: MutableMap<Class<*>, Set<Class<*>>> = mutableMapOf()
    val definitionInstanceList: MutableList<Any> = mutableListOf()
    val definitionAwareInstanceList: MutableList<Aware> = mutableListOf()
    val definitionContextListenerInstanceList: MutableList<PurpleContextListener> = mutableListOf()
    val definitionAnyInstanceList: MutableList<Any> = mutableListOf()
    val eventHandlerList: MutableList<EventHandler> = mutableListOf()

    val definitionInstanceMap: MutableMap<Any, PurpleBeanDefinition> = mutableMapOf()

    init {
        val exceptionHandler =
            DesignExceptionHandler()
        Thread.currentThread().uncaughtExceptionHandler = exceptionHandler
        val coroutineScope =
            CoroutineScope(Dispatchers.Main + EmptyCoroutineContext + exceptionHandler.coExceptionHandler)
        LocalPurpleCoroutineScope.provide(coroutineScope)
    }

    override fun onInit() {
        LocalPurpleEventPublisher.provide(this)
        DefinitionsCollector().collectDefinitions(this)
        DefinitionsInstantiator().doInitDefinitions(this)
        AnythingComponentLoader().loadComponents(this)
    }

    override fun onStart() {

    }

    override fun onRefresh() {
        publishEvent(NoticePurpleContextAwareEvent(this))
        publishEvent(PurpleInitEvent())
    }

    override fun onReady() {

    }

    override fun onStop() {

    }

    override fun onDestroy() {

    }

    fun <T> addInstance(instance: T) {

    }

    fun <T> getInstance(clazz: Class<T>): T? {
        return null
    }

    fun <T> getInstanceByName(name: String): T? {
        return null
    }

    protected fun <T> getAwareList(clazz: Class<T>): List<T> {
        return definitionAwareInstanceList.filterIsInstance(clazz)
    }

    protected fun getPurpleContextListenerList(): List<PurpleContextListener> {
        return definitionContextListenerInstanceList
    }

    override fun publishEvent(event: DesignEvent) {
        var eventOverride = event
        val eventHandlers = getEventHandlers()
        val eventPreHandlers = eventHandlers.filterIsInstance<EventPreHandler>()
        if (eventPreHandlers.isNotEmpty()) {
            eventPreHandlers.forEach {
                eventOverride = it.processEvent(eventOverride)
            }
        }
        onEvent(eventOverride)
        val eventPostHandlers = eventHandlers.filterIsInstance<EventPostHandler>()
        if (eventPostHandlers.isNotEmpty()) {
            eventPostHandlers.forEach {
                eventOverride = it.processEvent(eventOverride)
            }
        }
    }

    override fun onEvent(event: DesignEvent) {

    }

    private fun getEventHandlers(): List<EventHandler> {
        return eventHandlerList
    }

    override fun onTrimMemory(level: Int) {
        PurpleLogger.current.d(TAG, "onTrimMemory, level:$level")
    }

    override fun onLowMemory() {
        PurpleLogger.current.d(TAG, "onLowMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        PurpleLogger.current.d(TAG, "onConfigurationChanged, newConfig:$newConfig")
    }
}

