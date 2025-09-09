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

    private fun initAndroid() {
        PurpleLogger.current.d(TAG, "initAndroid")
        androidContexts.simpleInit()
        publishEvent(AndroidInitEvent())
    }

    override fun onInit() {
        super.onInit()
        PurpleLogger.current.d(TAG, "init")
        initAndroid()
    }

    override fun onStart() {
        PurpleLogger.current.d(TAG, "onStart")
        val startDateTime =
            SimpleDateFormat.getDateTimeInstance().format(Calendar.getInstance().time)
        val purpleStartEvent = PurpleStartEvent(startDateTime, "Context is healthy!")
        publishEvent(purpleStartEvent)
    }

    override fun onRefresh() {
        super.onRefresh()
        PurpleLogger.current.d(TAG, "onRefresh")
    }

    override fun onReady() {
        PurpleLogger.current.d(TAG, "onReady")
    }

    override fun onStop() {
        PurpleLogger.current.d(TAG, "onStop")
        val purpleStopEvent = PurpleStopEvent()
        publishEvent(purpleStopEvent)
    }

    override fun onDestroy() {
        PurpleLogger.current.d(TAG, "onDestroy")
    }

    override fun publishEvent(event: DesignEvent) {
        super.publishEvent(event)
        PurpleLogger.current.d(TAG, "publishEvent: event:$event")
    }

    override fun onEvent(event: DesignEvent) {
        super.onEvent(event)
        PurpleLogger.current.d(TAG, "onEvent: event:$event")
        dispatchEvent(event)
    }

    private fun dispatchEvent(event: DesignEvent) {
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

    private fun dispatchPurpleContextEvent(event: DesignEvent) {
        PurpleLogger.current.d(TAG, "dispatchPurpleContextEvent")
        val purpleContextListenerList = getPurpleContextListenerList()
        purpleContextListenerList.filterIsInstance<PurpleContextEventListener>().forEach {
            it.onEvent(event)
        }
    }

    private fun dispatchPurpleContextAware(event: NoticePurpleContextAwareEvent) {
        PurpleLogger.current.d(TAG, "dispatchPurpleContextAware")
        getAwareList(PurpleContextAware::class.java).forEach {
            it.setPurpleContext(event.purpleContext)
        }
    }
}

