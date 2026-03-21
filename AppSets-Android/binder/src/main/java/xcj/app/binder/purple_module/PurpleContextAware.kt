package xcj.app.module.binder.purple_module

import xcj.app.starter.test.PurpleContext

class PurpleContextAware : PurpleContextAware {
    private val TAG = "PurpleContextAware"
    override fun setPurpleContext(purpleContext: PurpleContext) {
        PurpleLogger.current.d(
            TAG,
            "xcj.app.module.binder.purple_context, purpleContxt:${purpleContext}"
        )
    }
}