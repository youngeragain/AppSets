package xcj.app.starter.test

import android.app.Application
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.DesignEvent
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * @param any any must be android.app.Application, else be error!
 */
class SimplePurpleForAndroidContext(any: Any) : PurpleContext() {

    companion object {
        private const val TAG = "SimplePurpleForAndroidContext"
    }

    private val androidContexts: AndroidContexts = AndroidContexts(any as Application)

    fun isApplicationInBackground(): Boolean {
        val applicationInBackground = androidContexts.isApplicationInBackground()
        PurpleLogger.current.d(TAG, "isApplicationInBackground, $applicationInBackground")
        return applicationInBackground
    }

    private suspend fun initAndroid() {
        PurpleLogger.current.d(TAG, "initAndroid")
        androidContexts.simpleInit()
        publishEvent(AndroidEvent("onApplicationCreated"))
    }

    override suspend fun onInit() {
        super.onInit()
        PurpleLogger.current.d(TAG, "init")
        initAndroid()
    }

    override suspend fun onStart() {
        PurpleLogger.current.d(TAG, "onStart")
        val startDateTime =
            SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
        val purpleStartEvent = PurpleStartEvent(startDateTime, "Context is healthy!")
        publishEvent(purpleStartEvent)
    }

    override suspend fun onRefresh() {
        super.onRefresh()
        PurpleLogger.current.d(TAG, "onRefresh")
    }

    override suspend fun onReady() {
        PurpleLogger.current.d(TAG, "onReady")
    }

    override suspend fun onStop() {
        PurpleLogger.current.d(TAG, "onStop")
        val purpleStopEvent = PurpleStopEvent()
        publishEvent(purpleStopEvent)
    }

    override suspend fun onDestroy() {
        PurpleLogger.current.d(TAG, "onDestroy")
    }

    override suspend fun publishEvent(event: DesignEvent) {
        super.publishEvent(event)
        PurpleLogger.current.d(TAG, "publishEvent: event:$event")
    }

    override suspend fun onEvent(event: DesignEvent) {
        super.onEvent(event)
        PurpleLogger.current.d(TAG, "onEvent: event:$event")
        dispatchEvent(event)
    }

    private suspend fun dispatchEvent(event: DesignEvent) {
        PurpleLogger.current.d(TAG, "dispatchEvent: event:$event")
        when (event) {
            is NoticePurpleContextAwareEvent -> {
                dispatchPurpleContextAware(event)
            }

            else -> {
                dispatchPurpleContextEvent(event)
            }
        }
    }

    private suspend fun dispatchPurpleContextEvent(event: DesignEvent) {
        PurpleLogger.current.d(TAG, "dispatchPurpleContextEvent")
        val purpleContextListenerList = getPurpleContextListenerList()
        purpleContextListenerList.filterIsInstance<PurpleContextEventListener>().forEach {
            it.onEvent(event)
        }
    }

    private suspend fun dispatchPurpleContextAware(event: NoticePurpleContextAwareEvent) {
        PurpleLogger.current.d(TAG, "dispatchPurpleContextAware")
        getAwareList(PurpleContextAware::class.java).forEach {
            it.setPurpleContext(event.purpleContext)
        }
    }
}

