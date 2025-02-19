package xcj.app.appsets.purple_module

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.PurpleContext
import xcj.app.starter.test.PurpleContextAware

class PurpleContextAware : PurpleContextAware {
    companion object {
        private const val TAG = "PurpleContextAware"
    }
    override fun setPurpleContext(purpleContext: PurpleContext) {
        PurpleLogger.current.d(
            TAG,
            "xcj.app.appsets.purple_module setPurpleContext, purpleContext:${purpleContext}"
        )
    }
}