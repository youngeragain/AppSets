package xcj.app.purple_module

import xcj.app.core.foundation.DesignEvent
import xcj.app.core.test.AndroidInitEvent
import xcj.app.core.test.PurpleContextEventListener

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