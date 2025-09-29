package xcj.app.starter.android

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.ViewModel

interface SystemContentSelectionCallback {
    fun autoRemove(): Boolean = true
    fun onSystemContentSelected(content: Any?)
}

interface ActivityThemeInterface {

    fun getSystemContentSelectionCallback(): SystemContentSelectionCallback?

    fun setSystemContentSelectionCallback(callback: SystemContentSelectionCallback?)

    fun <I, C : ActivityResultContract<I, *>> getActivityResultLauncher(
        contractClass: Class<C>,
        requestPrams: Any?,
    ): ActivityResultLauncher<I>? = null

    fun <V : ViewModel> requireViewModel(): V? = null

    fun isFitSystemWindow(): Boolean {
        return false
    }

    fun isOverrideSystemBarLightModel(): Boolean? {
        return null
    }

    fun isHideStatusBar(): Boolean {
        return false
    }

    fun isHideNavigationBar(): Boolean {
        return false
    }

    fun isLayoutInCutOut(): Boolean {
        return false
    }

    fun isKeepScreenOn(): Boolean {
        return false
    }

    fun setupSystemBars(isLight: Boolean) {

    }
}