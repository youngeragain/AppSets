package xcj.app.module.binder.purple_context

import xcj.app.starter.foundation.DesignEvent
import xcj.app.starter.test.PurpleContextEventListener
import xcj.app.starter.test.AndroidInitEvent

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