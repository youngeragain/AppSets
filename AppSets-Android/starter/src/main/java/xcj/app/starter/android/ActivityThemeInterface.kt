package xcj.app.starter.android

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel

interface ActivityThemeInterface {

    fun <I> getActivityResultLauncher(
        inputClazz: Class<I>,
        requestPrams:Any?,
    ): ActivityResultLauncher<*>? = null

    fun makeActivityResultLauncher() {

    }

    fun requireViewModel(): ViewModel? = null

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