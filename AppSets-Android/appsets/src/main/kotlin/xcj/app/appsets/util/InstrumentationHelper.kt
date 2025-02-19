package xcj.app.appsets.util

import android.accessibilityservice.AccessibilityService
import android.app.Instrumentation
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import xcj.app.starter.android.util.PurpleLogger

object InstrumentationHelper {
    private const val TAG = "InstrumentationHelper"
    const val BACK_CLICK = "BACK_CLICK"
    const val GO_HOME = "GO_HOME"

    private var mInstrumentation: Any? = null

    fun invoke(context: Context, name: String, vararg params: Any?) {
        if (context !is AppCompatActivity) {
            return
        }
        context.getSystemService(AccessibilityService::class.java)
        val instrumentation = getInstrumentationIfNeeded(context) ?: return
        when (name) {
            BACK_CLICK -> {
                callClickBack(instrumentation)
            }

            GO_HOME -> {
                callGoHome(instrumentation)
            }
        }
    }

    private fun callGoHome(instrumentation: Any) {
        if (instrumentation !is Instrumentation) {
            PurpleLogger.current.d(
                TAG,
                "callClickOnBack, failed, instrumentation is not Instrumentation, return!"
            )
            return
        }
        instrumentation.uiAutomation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    private fun callClickBack(instrumentation: Any) {
        if (instrumentation !is Instrumentation) {
            PurpleLogger.current.d(
                TAG,
                "callClickOnBack, failed, instrumentation is not Instrumentation, return!"
            )
            return
        }
    }

    private fun getInstrumentationIfNeeded(context: AppCompatActivity): Any? {
        if (mInstrumentation != null) {
            return mInstrumentation!!
        }
        runCatching {
            val contextThemeWrapper =
                context.baseContext as? androidx.appcompat.view.ContextThemeWrapper
            if (contextThemeWrapper == null) {
                PurpleLogger.current.d(
                    TAG,
                    "get Instrumentation failed!, context.baseContext is not ContextThemeWrapper"
                )
                return null
            }
            val baseContext = contextThemeWrapper.baseContext
            val activityThread = baseContext.javaClass.getDeclaredField("mMainThread").let {
                if (!it.isAccessible) {
                    it.isAccessible = true
                }
                it.get(baseContext)
            }
            activityThread.javaClass.getMethod("getInstrumentation").let {
                if (!it.isAccessible) {
                    it.isAccessible = true
                }
                it.invoke(activityThread)
            }
        }.onFailure {
            PurpleLogger.current.d(TAG, "get Instrumentation failed!, e:" + it.message)
            return null
        }.onSuccess {
            mInstrumentation = it
            return it
        }
        return null
    }
}