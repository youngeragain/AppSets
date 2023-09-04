package xcj.app.appsets.purple_module

import android.util.Log
import xcj.app.core.foundation.DesignEvent
import xcj.app.core.test.AndroidInitEvent
import xcj.app.core.test.PurpleContextEventListener

/**
 * 监听PurpleContext中AndroidInit方法完成的回调
 */
class AndroidInitEventListener:PurpleContextEventListener {
    private val TAG = "AndroidInitEventListener"
    override fun onEvent(event: DesignEvent) {
        Log.e(TAG, "xcj.app.appsets.purple_module onEvent, event:$event")
        if(event is AndroidInitEvent){
            ModuleRouter().initModule()
        }
    }
}