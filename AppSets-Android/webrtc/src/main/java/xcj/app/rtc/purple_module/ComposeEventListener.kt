package xcj.app.rtc.purple_module
import xcj.app.proxy.ui.purple_module.ComposeEventHandler
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.DesignEvent
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.PurpleContextEventListener

class ComposeEventListener : PurpleContextEventListener {
    companion object {
        private const val TAG = "ComposeEventListener"
    }

    override fun onEvent(event: DesignEvent) {
        PurpleLogger.current.d(
            TAG,
            "onEvent, event:$event"
        )
        if (event is ComposeEvent) {
            xcj.app.proxy.ui.purple_module.ComposeEventHandler().handleEvent(event)
        }
    }
}