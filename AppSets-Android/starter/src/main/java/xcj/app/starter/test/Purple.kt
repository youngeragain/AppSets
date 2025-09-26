package xcj.app.starter.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.starter.android.util.PurpleLogger

class Purple {

    companion object {
        const val TAG = "Purple"
    }

    /**
     * 避免在android application初始化处调用而导致启动时间过长
     */
    fun bootstrap(any: Any) {
        PurpleLogger.current.d(TAG, "bootstrap, any:$any")
        val timeMeasure = TimeMeasure()
        val androidApplicationClazz = Class.forName("android.app.Application")
        var simplePurpleContext: PurpleContext? = null
        if (androidApplicationClazz.isAssignableFrom(any::class.java)) {
            simplePurpleContext = SimplePurpleForAndroidContext(any)
        } else {
            PurpleLogger.current.d(
                TAG,
                "bootstrap, to do, make others platform is possible!"
            )
        }
        if (simplePurpleContext == null) {
            PurpleLogger.current.d(
                TAG,
                "bootstrap, simplePurpleContext is null, return."
            )
            return
        }

        LocalPurple.provide(simplePurpleContext)

        LocalPurpleCoroutineScope.current.launch(Dispatchers.IO) {
            simplePurpleContext.onInit()
            simplePurpleContext.onStart()
            simplePurpleContext.onRefresh()
            simplePurpleContext.onReady()
            PurpleLogger.current.d(
                TAG,
                "prepare for ready time spend:${timeMeasure.snapshot().latestDiff()}ms"
            )
        }
    }
}