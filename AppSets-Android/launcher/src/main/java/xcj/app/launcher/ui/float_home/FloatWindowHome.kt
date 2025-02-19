package xcj.app.launcher.ui.float_home

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import xcj.app.starter.android.util.PurpleLogger

class FloatWindowHome(private val savedStateRegistryOwner: SavedStateRegistryOwner) :
    FloatWindowInterface {
    companion object {
        private const val TAG = "FloatWindowHome"
    }

    private lateinit var floatWindowHomeViewState: FloatWindowHomeViewState
    private lateinit var windowManager: WindowManager
    private lateinit var mWindowView: ComposeView

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initWindow(context: Context) {
        if (!::windowManager.isInitialized) {
            windowManager = context.getSystemService(WindowManager::class.java)
            PurpleLogger.current.d(
                TAG,
                "WindowMetrics:${windowManager.maximumWindowMetrics.bounds}"
            )
        }
        if (!::floatWindowHomeViewState.isInitialized) {
            floatWindowHomeViewState = FloatWindowHomeViewState()
        }
        initWindowView(context)
        observeFloatWindowState()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeFloatWindowState() {
        PurpleLogger.current.d(TAG, "observeFloatWindowState")
        floatWindowHomeViewState.isShowingState.observe(savedStateRegistryOwner) { isShow ->
            PurpleLogger.current.d(
                TAG,
                "observeFloatWindowState:isShowingState, isShow:$isShow"
            )
            if (isShow && !floatWindowHomeViewState.isViewAdded) {
                if (floatWindowHomeViewState.isAnimate) {
                    mWindowView.scaleX = 0f
                    mWindowView.scaleY = 0f
                    mWindowView.alpha = 0f
                }
                windowManager.addView(mWindowView, mWindowView.layoutParams)
            } else if (!isShow && floatWindowHomeViewState.isViewAdded) {
                if (floatWindowHomeViewState.isAnimate) {
                    mWindowView.animate().alpha(0f).scaleX(0f).scaleY(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(350)
                        .withEndAction {
                            windowManager.removeViewImmediate(mWindowView)
                        }.start()
                } else {
                    windowManager.removeViewImmediate(mWindowView)
                }
            }
        }
        floatWindowHomeViewState.windowOffset.observe(savedStateRegistryOwner) { offset ->
            if (offset == null || !floatWindowHomeViewState.isShowing) {
                return@observe
            }
            PurpleLogger.current.d(TAG, "observeFloatWindowState:offset:$offset")
            val layoutParams = mWindowView.layoutParams as WindowManager.LayoutParams
            var update = false
            val tempTargetOffsetX = layoutParams.x + offset.x
            if (tempTargetOffsetX <= 0 || tempTargetOffsetX <= windowManager.maximumWindowMetrics.bounds.width()) {
                PurpleLogger.current.d(TAG, "Horizontal edge reached!")
            } else {
                layoutParams.x = offset.x
                update = true

            }
            if (layoutParams.y >= 0 && layoutParams.y <= windowManager.maximumWindowMetrics.bounds.height()) {
                layoutParams.y = offset.y
                update = true
            } else {
                PurpleLogger.current.d(TAG, "Vertical edge reached!")
            }
            if (update) {
                windowManager.updateViewLayout(mWindowView, layoutParams)
            }
        }
    }

    private fun invalidateCompose(composeView: ComposeView) {
        composeView.setContent {
            FlotWindowCompose(
                onDragGesture = floatWindowHomeViewState::updateWindowOffset
            )
        }
    }

    override fun show(withAnimation: Boolean) {
        PurpleLogger.current.d(TAG, "show")
        if (!preCheckCondition()) {
            return
        }
        floatWindowHomeViewState.show(withAnimation)
    }

    private fun preCheckCondition(): Boolean {
        if (!::windowManager.isInitialized) {
            PurpleLogger.current.d(
                TAG,
                "preCheckCondition, windowManager is null!"
            )
            return false
        }
        if (!::mWindowView.isInitialized) {
            PurpleLogger.current.d(TAG, "preCheckCondition, windowView is null!")
            return false
        }
        if (!::floatWindowHomeViewState.isInitialized) {
            PurpleLogger.current.d(
                TAG,
                "preCheckCondition, floatWindowViewState is null!"
            )
            return false
        }
        return true
    }


    override fun hide(withAnimation: Boolean) {
        PurpleLogger.current.d(TAG, "hide")
        if (!preCheckCondition()) {
            return
        }
        floatWindowHomeViewState.hide(withAnimation)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initWindowView(context: Context) {
        if (!::mWindowView.isInitialized) {
            mWindowView = ComposeView(context).apply {
                val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                }
                val dp350InPixel = (350 * (context.resources.displayMetrics.densityDpi / 160))
                layoutParams = WindowManager.LayoutParams(
                    dp350InPixel,
                    dp350InPixel,
                    windowType,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSPARENT
                ).apply {
                    gravity = Gravity.START or Gravity.TOP
                    x = ((windowManager.maximumWindowMetrics.bounds.width() - dp350InPixel) / 2)
                    y = ((windowManager.maximumWindowMetrics.bounds.height() - dp350InPixel) / 2)
                    floatWindowHomeViewState.updateWindowOffset(Offset(x.toFloat(), y.toFloat()))
                }
                PurpleLogger.current.d(TAG, "layoutParams:$layoutParams")
                addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(view: View) {
                        if (floatWindowHomeViewState.isAnimate) {
                            view.animate().alpha(1f).scaleX(1f).scaleY(1f)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .setDuration(350)
                                .withEndAction {
                                    floatWindowHomeViewState.isViewAdded = true
                                    invalidateCompose(view as ComposeView)
                                }.start()
                        } else {
                            floatWindowHomeViewState.isViewAdded = true
                            invalidateCompose(view as ComposeView)
                        }
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        floatWindowHomeViewState.isViewAdded = false
                    }
                })
            }
        }
        val decorView = mWindowView
        if (decorView.findViewTreeLifecycleOwner() == null) {
            decorView.setViewTreeLifecycleOwner(savedStateRegistryOwner)
        }
        if (decorView.findViewTreeSavedStateRegistryOwner() == null) {
            decorView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
        }
    }
}
