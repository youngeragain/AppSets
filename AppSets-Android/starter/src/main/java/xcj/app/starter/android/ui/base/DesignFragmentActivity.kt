package xcj.app.starter.android.ui.base

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.util.ThemeUtil

abstract class DesignFragmentActivity :
    FragmentActivity(), ActivityThemeInterface {
    companion object {
        private const val TAG = "BaseFragmentActivity"
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeUtil.onCreate(
            this,
            isHideStatusBar(),
            isHideNavigationBar(),
            isFitSystemWindow(),
            isOverrideSystemBarLightModel()
        )

        if (isKeepScreenOn()) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        if (isLayoutInCutOut()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val params = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
                window.attributes = params
            }
        }

        makeActivityResultLauncher()
    }

    override fun onResume() {
        ThemeUtil.onResume(
            this,
            isHideStatusBar(),
            isHideNavigationBar(),
            isFitSystemWindow(),
            isOverrideSystemBarLightModel()
        )
        super.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    override fun setupSystemBars(isLight: Boolean) {
        ThemeUtil.setUpSystemBars(
            this,
            isHideStatusBar(),
            isHideNavigationBar(),
            isFitSystemWindow(),
            isLight
        )
    }

    override fun <I> getActivityResultLauncher(
        inputClazz: Class<I>,
        requestPrams: Any?
    ): ActivityResultLauncher<*>? {
        return null
    }

    override fun makeActivityResultLauncher() {

    }
}