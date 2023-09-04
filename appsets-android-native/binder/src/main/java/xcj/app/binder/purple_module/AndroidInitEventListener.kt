package xcj.app.module.binder.purple_context

import xcj.app.core.foundation.DesignEvent
import xcj.app.core.test.PurpleContextEventListener
import xcj.app.core.test.AndroidInitEvent

/**
 * 监听PurpleContext中AndroidInit方法完成的回调
 */
class AndroidInitEventListener:PurpleContextEventListener {
    private val TAG = "AndroidInitEventListener"
    override fun onEvent(event: DesignEvent) {
        if(event is AndroidInitEvent){
            ModuleRouter().initModule()
        }
    }
}