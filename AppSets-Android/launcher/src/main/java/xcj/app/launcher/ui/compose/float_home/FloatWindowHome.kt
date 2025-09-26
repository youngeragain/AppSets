package xcj.app.launcher.ui.compose.float_home

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
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import xcj.app.starter.android.util.PurpleLogger

class FloatWindowHome(
    private val savedStateRegistryOwner: SavedStateRegistryOwner
) :
    FloatWindowInterface {
    companion object {
        private const val TAG = "FloatWindowHome"
    }

    private var mWindowViewState: FloatWindowHomeViewState? = null
    private var mWindowManager: WindowManager? = null
    private var mWindowView: ComposeView? = null

    @RequiresApi(Build.VERSION_CODES.R)
    override fun initWindow(context: Context) {
        if (mWindowManager == null) {
            mWindowManager = ContextCompat.getSystemService(context, WindowManager::class.java)
            PurpleLogger.current.d(
                TAG,
                "WindowMetrics:${mWindowManager?.maximumWindowMetrics?.bounds}"
            )
        }
        initWindowView(context)
        observeFloatWindowState()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun observeFloatWindowState() {
        PurpleLogger.current.d(TAG, "observeFloatWindowState")
        val floatWindowHomeViewState = mWindowViewState
        if (floatWindowHomeViewState == null) {
            return
        }
        floatWindowHomeViewState.isShowingState.observe(savedStateRegistryOwner) { isShow ->
            PurpleLogger.current.d(
                TAG,
                "observeFloatWindowState:isShowingState, isShow:$isShow"
            )
            val windowManager = mWindowManager
            if (windowManager == null) {
                return@observe
            }
            val windowView = mWindowView
            if (windowView == null) {
                return@observe
            }
            if (isShow && !floatWindowHomeViewState.isViewAdded) {
                if (floatWindowHomeViewState.isAnimate) {
                    windowView.scaleX = 0f
                    windowView.scaleY = 0f
                    windowView.alpha = 0f
                }
                windowManager.addView(windowView, windowView.layoutParams)
            } else if (!isShow && floatWindowHomeViewState.isViewAdded) {
                if (floatWindowHomeViewState.isAnimate) {
                    windowView.animate().alpha(0f).scaleX(0f).scaleY(0f)
                        .setInterpolator(AccelerateDecelerateInterpolator()).setDuration(350)
                        .withEndAction {
                            windowManager.removeViewImmediate(windowView)
                        }.start()
                } else {
                    windowManager.removeViewImmediate(windowView)
                }
            }
        }
        floatWindowHomeViewState.windowOffset.observe(savedStateRegistryOwner) { offset ->
            val windowManager = mWindowManager
            if (windowManager == null) {
                return@observe
            }
            val windowView = mWindowView
            if (windowView == null) {
                return@observe
            }
            if (offset == null || !floatWindowHomeViewState.isShowing) {
                return@observe
            }
            PurpleLogger.current.d(TAG, "observeFloatWindowState:offset:$offset")
            val layoutParams = windowView.layoutParams as WindowManager.LayoutParams
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
                windowManager.updateViewLayout(windowView, layoutParams)
            }
        }
    }

    private fun invalidateCompose(composeView: ComposeView) {
        composeView.setContent {
            FlotWindowCompose(
                onDragGesture = {
                    mWindowViewState?.updateWindowOffset(it)
                }
            )
        }
    }

    override fun show(withAnimation: Boolean) {
        PurpleLogger.current.d(TAG, "show")
        if (!preCheckCondition()) {
            return
        }
        mWindowViewState?.show(withAnimation)
    }

    private fun preCheckCondition(): Boolean {
        if (mWindowManager == null) {
            PurpleLogger.current.d(
                TAG,
                "preCheckCondition, windowManager is null!"
            )
            return false
        }
        if (mWindowView == null) {
            PurpleLogger.current.d(TAG, "preCheckCondition, windowView is null!")
            return false
        }
        if (mWindowViewState == null) {
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
        mWindowViewState?.hide(withAnimation)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initWindowView(context: Context) {
        val windowManager = mWindowManager
        if (windowManager == null) {
            return
        }
        if (mWindowViewState == null) {
            mWindowViewState = FloatWindowHomeViewState()
        }

        if (mWindowView == null) {
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
                }
                PurpleLogger.current.d(TAG, "layoutParams:$layoutParams")
                addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(view: View) {
                        val windowViewState = mWindowViewState
                        if (windowViewState == null) {
                            return
                        }
                        if (windowViewState.isAnimate) {
                            view.animate().alpha(1f).scaleX(1f).scaleY(1f)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .setDuration(350)
                                .withEndAction {
                                    windowViewState.isViewAdded = true
                                    invalidateCompose(view as ComposeView)
                                }.start()
                        } else {
                            windowViewState.isViewAdded = true
                            invalidateCompose(view as ComposeView)
                        }
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        val windowViewState = mWindowViewState
                        if (windowViewState == null) {
                            return
                        }
                        windowViewState.isViewAdded = false
                    }
                })
            }
        }
        val windowView = mWindowView
        if (windowView != null) {
            if (windowView.findViewTreeLifecycleOwner() == null) {
                windowView.setViewTreeLifecycleOwner(savedStateRegistryOwner)
            }
            if (windowView.findViewTreeSavedStateRegistryOwner() == null) {
                windowView.setViewTreeSavedStateRegistryOwner(savedStateRegistryOwner)
            }
            mWindowViewState?.updateWindowOffset(Offset(windowView.x, windowView.y))
        }
    }
}
