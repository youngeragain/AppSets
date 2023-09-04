package xcj.app.compose_share.purple_module

import android.util.Log
import xcj.app.core.test.PurpleContext
import xcj.app.core.test.PurpleContextAware

class PurpleContextAware : PurpleContextAware {
    private val TAG = "PurpleContextAware"
    override fun setPurpleContext(purpleContext: PurpleContext) {
        Log.e(
            TAG,
            "xcj.app.compose_share.purple_module setPurpleContext, purpleContext:$purpleContext"
        )
    }
}