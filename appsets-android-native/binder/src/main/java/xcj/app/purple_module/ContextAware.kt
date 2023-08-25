package xcj.app.module.binder

import android.util.Log
import xcj.app.core.test.PurpleContext
import xcj.app.core.test.PurpleContextAware

class ContextAware:PurpleContextAware {
    override fun setPurpleContext(purpleContext: PurpleContext) {
        Log.i("ContextAware", "binder:setAAContext:${purpleContext.androidContexts}")
    }
}