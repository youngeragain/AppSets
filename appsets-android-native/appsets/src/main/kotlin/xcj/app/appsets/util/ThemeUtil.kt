package xcj.app.appsets.util

import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.purple_module.MySharedPreferences


class ThemeUtil {
    private lateinit var windowInsetsControllerCompat: WindowInsetsControllerCompat
    private var currentTheme: Int = Configuration.UI_MODE_NIGHT_NO
    fun changeUIMode(activity: AppCompatActivity): Int {
        currentTheme = MySharedPreferences.getInt(Constants.APP_THEME)
        if (currentTheme == -1) {
            currentTheme = Configuration.UI_MODE_NIGHT_NO
        }

        return when (currentTheme) {
            Configuration.UI_MODE_NIGHT_NO -> {
                MySharedPreferences.putInt(Constants.APP_THEME, Configuration.UI_MODE_NIGHT_YES)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    activity.delegate.localNightMode = MODE_NIGHT_YES
                    activity.recreate()
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                }
                Configuration.UI_MODE_NIGHT_YES
            }

            else -> {
                MySharedPreferences.putInt(Constants.APP_THEME, Configuration.UI_MODE_NIGHT_NO)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    activity.delegate.localNightMode = MODE_NIGHT_NO
                    activity.recreate()
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }
                Configuration.UI_MODE_NIGHT_NO
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onResume(activity: AppCompatActivity, hideStatusBar: Boolean, hideNavigationBar: Boolean) {

        setUpSystemBar(activity, hideStatusBar, hideNavigationBar)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onCreate(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowInsetsControllerCompat =
                WindowInsetsControllerCompat(activity.window, activity.window.decorView)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpSystemBar(
        activity: AppCompatActivity,
        hideStatusBar: Boolean,
        hideNavigationBar: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.isImmersive = true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                /*windowInsetsControllerCompat.isAppearanceLightStatusBars = true
                windowInsetsControllerCompat.isAppearanceLightNavigationBars = true*/
                WindowCompat.setDecorFitsSystemWindows(activity.window, false)
                if (!hideStatusBar && !hideNavigationBar)
                    return
                windowInsetsControllerCompat.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                if (::windowInsetsControllerCompat.isInitialized) {
                    if (hideStatusBar) {
                        windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars())
                    }
                    if (hideNavigationBar) {
                        windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.navigationBars())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } else {
            val lightFlag: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            val darkFlag: Int = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            activity.window.decorView.systemUiVisibility =
                when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_NO -> lightFlag
                    else -> darkFlag
                }
        }

    }
}