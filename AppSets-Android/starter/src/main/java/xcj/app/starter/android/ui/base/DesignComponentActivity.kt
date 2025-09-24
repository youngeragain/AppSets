package xcj.app.starter.android.ui.base

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.util.ThemeUtil

abstract class DesignComponentActivity :
    ComponentActivity(), ActivityThemeInterface, DesignInterface {
    companion object {
        private const val TAG = "BaseActivity"
        const val CREATE_BY_DEFAULT = "DEFAULT"
        const val CREATE_BY_REFLECTION = "REFLECTION"
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
            this
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
    }

    override fun onResume() {
        ThemeUtil.onResume(
            this,
            this
        )
        super.onResume()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    override fun setupSystemBars(isLight: Boolean) {
        ThemeUtil.setUpSystemBars(
            this,
            this
        )
    }

    override fun <I> getActivityResultLauncher(
        inputClazz: Class<I>,
        requestPrams: Any?
    ): ActivityResultLauncher<I>? {
        return null
    }
}