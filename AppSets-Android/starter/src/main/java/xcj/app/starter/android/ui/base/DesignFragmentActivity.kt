package xcj.app.starter.android.ui.base

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.FragmentActivity
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.SystemContentSelectionCallback
import xcj.app.starter.android.util.ThemeUtil

abstract class DesignFragmentActivity :
    FragmentActivity(), ActivityThemeInterface {
    companion object {
        private const val TAG = "BaseFragmentActivity"
    }

    private var systemContentSelectionCallback: SystemContentSelectionCallback? = null

    override fun setSystemContentSelectionCallback(callback: SystemContentSelectionCallback?) {
        this.systemContentSelectionCallback = callback
    }

    override fun getSystemContentSelectionCallback(): SystemContentSelectionCallback? {
        return systemContentSelectionCallback
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

    override fun <I, C : ActivityResultContract<I, *>> getActivityResultLauncher(
        contractClass: Class<C>,
        requestPrams: Any?
    ): ActivityResultLauncher<I>? {
        return null
    }
}