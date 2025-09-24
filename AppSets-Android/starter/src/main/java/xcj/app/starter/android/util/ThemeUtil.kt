package xcj.app.starter.android.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import xcj.app.starter.android.ActivityThemeInterface

object ThemeUtil {

    private const val TAG = "ThemeUtil"

    @JvmStatic
    fun onCreate(
        activity: ComponentActivity,
        activityThemeInterface: ActivityThemeInterface,
    ) {
        setUpSystemBars(
            activity,
            activityThemeInterface
        )
    }

    @JvmStatic
    fun onResume(
        activity: ComponentActivity,
        activityThemeInterface: ActivityThemeInterface
    ) {
        setUpSystemBars(
            activity,
            activityThemeInterface
        )
    }


    @JvmStatic
    fun setUpSystemBars(
        activity: ComponentActivity,
        activityThemeInterface: ActivityThemeInterface,
    ) {
        activity.enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
        val windowInsetsControllerCompat =
            WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        if (activityThemeInterface.isHideStatusBar()) {
            windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars())
        }
        if (activityThemeInterface.isHideNavigationBar()) {
            windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.navigationBars())
        }
    }

    @JvmStatic
    private fun isSystemInDarkMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}