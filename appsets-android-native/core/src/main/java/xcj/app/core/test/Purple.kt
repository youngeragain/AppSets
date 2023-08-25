package xcj.app.core.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


object Purple {
    private lateinit var purpleContext: PurpleContext
    private lateinit var nativePurple: NativePurple
    fun getPurpleContext(): PurpleContext {
        return purpleContext
    }

    /**
     * 避免在android application初始化处调用而导致启动时间过长
     */
    fun bootstrap(any: Any) {
        val mainThreadExceptionHandler =
            DesignApplicationMainThreadExceptionHandler()
        Thread.currentThread().uncaughtExceptionHandler =
            mainThreadExceptionHandler
        val startTimeMills = System.currentTimeMillis()
        if (::purpleContext.isInitialized)
            return
        val androidApplicationClazz = Class.forName("android.app.Application")
        var simplePurpleContext: PurpleContext? = null
        if (androidApplicationClazz.isAssignableFrom(any::class.java)) {
            simplePurpleContext = SimplePurpleForAndroidContext(any)
        } else {
            //TODO
        }
        if(simplePurpleContext==null)
            return
        nativePurple = NativePurple()
        purpleContext = simplePurpleContext
        simplePurpleContext.coroutineScope.launch(Dispatchers.IO) {
            simplePurpleContext.init()
            simplePurpleContext.start()
            simplePurpleContext.started()
            simplePurpleContext.refresh()
            simplePurpleContext.ready()
            println("Purple: prepare for ready time spend:${System.currentTimeMillis() - startTimeMills} ms")
            simplePurpleContext.stop()
            simplePurpleContext.stopped()
            simplePurpleContext.destroy()
        }

    }
}




