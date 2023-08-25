package xcj.app.module.binder

import xcj.app.core.foundation.DesignEvent
import xcj.app.core.test.PurpleContextEventListener
import xcj.app.core.test.AndroidInitEvent

/**
 * 监听AAContext中AndroidInit方法完成的回调
 */
class AndroidInitEventListener:PurpleContextEventListener {
    override fun onEvent(event: DesignEvent) {
        if(event is AndroidInitEvent){
            ModuleRouter().initModule()
        }
    }
}