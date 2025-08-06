package xcj.app.starter.android.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

object ThemeUtil {

    private const val TAG = "ThemeUtil"

    @JvmStatic
    fun onCreate(
        activity: ComponentActivity,
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
        activity: ComponentActivity,
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
        activity: ComponentActivity,
        hideStatusBar: Boolean,
        hideNavigationBar: Boolean,
        fitSystemWindow: Boolean,
        overrideSystemBarLightModel: Boolean? = null
    ) {
        activity.enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
    }

    @JvmStatic
    private fun isSystemInDarkMode(context: Context): Boolean {
        val currentNightMode =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}