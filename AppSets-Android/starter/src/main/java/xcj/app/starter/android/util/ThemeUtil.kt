package xcj.app.starter.android.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object ThemeUtil {

    private const val TAG = "ThemeUtil"

    @JvmStatic
    fun onCreate(
        activity: Activity,
        hideStatusBar: Boolean,
        hideNavigationBar: Boolean,
        fitSystemWindow: Boolean,
        overrideSystemBarLightModel: Boolean? = null
    ) {
        setUpSystemBars(
            activity,
            hideStatusBar,
            hideNavigationBar,
            fitSystemWindow,
            overrideSystemBarLightModel
        )
    }

    @JvmStatic
    fun onResume(
        activity: Activity,
        hideStatusBar: Boolean,
        hideNavigationBar: Boolean,
        fitSystemWindow: Boolean,
        overrideSystemBarLightModel: Boolean? = null
    ) {
        setUpSystemBars(
            activity,
            hideStatusBar,
            hideNavigationBar,
            fitSystemWindow,
            overrideSystemBarLightModel
        )
    }


    @JvmStatic
    fun setUpSystemBars(
        activity: Activity,
        hideStatusBar: Boolean,
        hideNavigationBar: Boolean,
        fitSystemWindow: Boolean,
        overrideSystemBarLightModel: Boolean? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.isImmersive = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val windowInsetsControllerCompat =
                    WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                windowInsetsControllerCompat.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                var light = !isSystemInDarkMode(activity)
                if (overrideSystemBarLightModel != null) {
                    light = overrideSystemBarLightModel
                }
                windowInsetsControllerCompat.isAppearanceLightStatusBars = light
                windowInsetsControllerCompat.isAppearanceLightNavigationBars = light
                WindowCompat.setDecorFitsSystemWindows(activity.window, fitSystemWindow)
                if (hideStatusBar) {
                    windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars())
                }
                if (hideNavigationBar) {
                    windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.navigationBars())
                }
            } catch (e: Exception) {
                PurpleLogger.current.d(TAG, "setUpSystemBar, exception:${e.message}")
            }

        } else {
            activity.window.decorView.fitsSystemWindows = fitSystemWindow
            activity.window.decorView.systemUiVisibility =
                when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> {
                        var lightFlag: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            lightFlag = lightFlag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                        lightFlag
                    }

                    else -> {
                        val darkFlag: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        darkFlag
                    }
                }
        }

    }

    @JvmStatic
    private fun isSystemInDarkMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}