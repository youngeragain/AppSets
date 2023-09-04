package xcj.app.module.binder.purple_context

import android.util.Log
import xcj.app.core.test.PurpleContext
import xcj.app.core.test.PurpleContextAware

class PurpleContextAware : PurpleContextAware {
    private val TAG = "PurpleContextAware"
    override fun setPurpleContext(purpleContext: PurpleContext) {
        Log.i(TAG, "xcj.app.module.binder.purple_context, purpleContxt:${purpleContext}")
    }
}