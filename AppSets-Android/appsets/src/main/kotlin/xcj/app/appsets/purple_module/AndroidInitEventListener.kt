package xcj.app.appsets.purple_module

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.DesignEvent
import xcj.app.starter.test.AndroidEvent
import xcj.app.starter.test.PurpleContextEventListener

/**
 * 监听PurpleContext中AndroidInit方法完成的回调
 */
class AndroidInitEventListener : PurpleContextEventListener {
    companion object {
        private const val TAG = "AndroidInitEventListener"
    }

    override fun onEvent(event: DesignEvent) {
        PurpleLogger.current.d(
            TAG,
            "onEvent, event:$event"
        )
        if (event is AndroidEvent) {
            PurpleModule().onAndroidEvent(event)
        }
    }
}