package xcj.app.compose_share.purple_module

import android.util.Log
import xcj.app.core.foundation.DesignEvent
import xcj.app.core.test.PurpleContextEventListener

class AndroidInitEventListener : PurpleContextEventListener {
    private val TAG = "AndroidInitEventListener"
    override fun onEvent(event: DesignEvent) {
        Log.e(TAG, "xcj.app.compose_share.purple_module onEvent, event:$event")
    }
}