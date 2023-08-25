package xcj.app.core.test

import android.app.Application
import xcj.app.core.android.ApplicationHelper
import xcj.app.core.foundation.DesignEvent
import xcj.app.core.foundation.InternalSingletonHolder
import java.util.Calendar

/**
 * @param any any must be android.app.Application, else be error!
 */
class SimplePurpleForAndroidContext(any: Any) : PurpleContext() {
    val androidContexts: AndroidContexts = AndroidContexts((any as Application))
    private fun initAndroid() {
        androidContexts.simpleInit()
        ApplicationHelper.applicationInit(
            androidContexts.application,
            coroutineScope,
            androidContexts.getContextFileDir()
        )
        dispatchEvent(AndroidInitEvent())
    }

    override fun init() {
        DefinitionsCollector().collectDefinitions(this)
        DefinitionsInstantiator().doInitDefiniations(this)
        initAndroid()
        dispatchPurpleContextAware()
        dispatchEvent(PurpleInitEvent())
    }

    override fun start(){
        val startDateTime = InternalSingletonHolder.simpleDateFormat.format(Calendar.getInstance().time)
        val purpleStartEvent = PurpleStartEvent(startDateTime, "Context is healthy!")
        dispatchEvent(purpleStartEvent)
    }

    override fun started(){

    }

    override fun stop() {
        val purpleStopEvent = PurpleStopEvent()
        dispatchEvent(purpleStopEvent)
    }

    override fun stopped() {

    }

    override fun destroy() {

    }

    override fun refresh() {

    }

    override fun ready() {

    }

    private fun dispatchPurpleContextAware() {
        getAwareList(PurpleContextAware::class.java).forEach {
            it.setPurpleContext(this)
        }
    }

    private fun dispatchEvent(event: DesignEvent) {
        val purpleContextListenerList = getPurpleContextListenerList()
        purpleContextListenerList.filterIsInstance<PurpleContextEventListener>().forEach {
            it.onEvent(event)
        }
    }
}

