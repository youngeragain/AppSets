@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.util.ktx

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.view.updateLayoutParams
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import androidx.media3.ui.TimeBar
import androidx.media3.ui.TimeBar.OnScrubListener
import xcj.app.appsets.util.reflect.getField
import xcj.app.appsets.util.reflect.getFieldValue
import xcj.app.starter.android.util.PurpleLogger
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArraySet

private const val TAG = "PlayerViewKTX"

fun PlayerView.updateBottomControllerViewBackgroundColor(color: Int = Color.TRANSPARENT) {
    findViewById<View>(R.id.exo_bottom_bar).setBackgroundColor(
        color
    )
}

fun PlayerView.updateControllerViewSize(viewId: Int, size: Int) {
    findViewById<View>(viewId).updateLayoutParams<ViewGroup.LayoutParams> {
        width = size
        height = size
    }
}

fun PlayerView.updateControllerImageButtonResource(viewId: Int, resourceId: Int) {
    findViewById<ImageButton>(viewId).setImageResource(resourceId)
}

@UnstableApi
fun PlayerView.updateBottomControllerViewVisibility(viewId: Int, visibility: Int) {

    val controlView = this.getFieldValue<PlayerView, PlayerControlView>("controller")
    if (controlView == null) {
        return
    }
    val exoSettingsButton = controlView.findViewById<View>(viewId)
    if (exoSettingsButton == null) {
        return
    }
    val controlViewLayoutManager =
        controlView.getFieldValue<PlayerControlView, Any>("controlViewLayoutManager")

    if (controlViewLayoutManager == null) {
        return
    }
    runCatching {
        val showButtonMethod =
            controlViewLayoutManager::class.java.declaredMethods.firstOrNull {
                it.parameterTypes.size == 2 && it.parameterTypes[0] == View::class.java && it.parameterTypes[1] == Boolean::class.java
            }

        showButtonMethod?.invoke(
            controlViewLayoutManager,
            exoSettingsButton,
            visibility == View.VISIBLE
        )
    }.onFailure {
        PurpleLogger.current.d(
            TAG,
            "updateBottomControllerViewVisibility exception:${it.message}"
        )
    }
}

@UnstableApi
fun PlayerView.setProgressUpdateListener(progressUpdateListener: PlayerControlView.ProgressUpdateListener) {
    val controlView = this.getFieldValue<PlayerView, PlayerControlView>("controller")
    if (controlView == null) {
        return
    }
    controlView.setProgressUpdateListener(progressUpdateListener)
}

/**
 * durationView
 */
@UnstableApi
fun <V : View> PlayerView.getControllerViewForName(fieldName: String): V? {
    val controlView = this.getFieldValue<PlayerView, PlayerControlView>("controller")
    if (controlView == null) {
        return null
    }
    val field = getField<PlayerControlView>(fieldName)
    if (field == null) {
        return null
    }
    runCatching {
        return field.get(controlView) as? V
    }.onFailure {
        PurpleLogger.current.d(
            TAG,
            "getControllerViewForName:$fieldName exception:${it.message}"
        )
    }
    return null
}

@UnstableApi
fun PlayerView.setControllerDrawableFor(fieldName: String, drawable: Drawable?) {
    if (drawable == null) {
        return
    }
    val controlView = this.getFieldValue<PlayerView, PlayerControlView>("controller")
    if (controlView == null) {
        return
    }
    val field = getField<PlayerControlView>(fieldName)
    if (field == null) {
        return
    }
    val accessFlagsField = getField<Field>("accessFlags")
    if (accessFlagsField == null) {
        return
    }
    runCatching {
        accessFlagsField.setInt(field, field.modifiers and Modifier.FINAL.inv())
        field.set(controlView, drawable)
    }.onFailure {
        it.printStackTrace()
        PurpleLogger.current.d(
            TAG,
            "setDrawableFor exception:${it.message}"
        )
    }
}

@UnstableApi
fun PlayerView.removeControllerViewFor(vararg ids: Int) {
    val controlView = this.getFieldValue<PlayerView, PlayerControlView>("controller")
    if (controlView == null) {
        return
    }
    runCatching {
        ids.forEach { id ->
            controlView.findViewById<View>(id)?.let {
                (it.parent as? ViewGroup)?.removeView(it)
            }
        }
    }.onFailure {
        it.printStackTrace()
        PurpleLogger.current.d(
            TAG,
            "proxyTimeBar exception:${it.message}"
        )
    }
}

@UnstableApi
fun PlayerView.proxyBufferView(bufferView: View) {
    val bufferingViewField = getField<PlayerView>("bufferingView")
    if (bufferingViewField == null) {
        return
    }
    val accessFlagsField = getField<Field>("accessFlags")
    if (accessFlagsField == null) {
        return
    }
    runCatching {
        accessFlagsField.setInt(
            bufferingViewField,
            bufferingViewField.modifiers and Modifier.FINAL.inv()
        )
        val exoBufferView = bufferingViewField.get(this) as? View

        if (exoBufferView != null) {
            (exoBufferView.parent as? ViewGroup)?.let { parent ->
                val exoBufferViewLayoutParams = exoBufferView.layoutParams
                bufferView.layoutParams = exoBufferViewLayoutParams
                val exoTimeBarIndex = parent.indexOfChild(bufferView)
                parent.removeView(bufferView)
                parent.addView(bufferView, exoTimeBarIndex)
            }
        }
        bufferingViewField.set(this, bufferView)
    }.onFailure {
        it.printStackTrace()
        PurpleLogger.current.d(
            TAG,
            "proxyTimeBar exception:${it.message}"
        )
    }
}

@UnstableApi
fun PlayerView.proxyTimeBar(timeBar: View) {
    val controlView = this.getFieldValue<PlayerView, PlayerControlView>("controller")
    if (controlView == null) {
        return
    }
    val timeBarField = getField<PlayerControlView>("timeBar")
    if (timeBarField == null) {
        return
    }
    val controlViewLayoutManager =
        controlView.getFieldValue<PlayerControlView, Any>("controlViewLayoutManager")

    if (controlViewLayoutManager == null) {
        return
    }

    val timeBarInManagerField = getField(controlViewLayoutManager.javaClass, "timeBar")

    if (timeBarInManagerField == null) {
        return
    }

    val accessFlagsField = getField<Field>("accessFlags")
    if (accessFlagsField == null) {
        return
    }
    runCatching {
        accessFlagsField.setInt(
            timeBarField,
            timeBarField.modifiers and Modifier.FINAL.inv()
        )

        accessFlagsField.setInt(
            timeBarInManagerField,
            timeBarInManagerField.modifiers and Modifier.FINAL.inv()
        )
        val exoTimeBar = timeBarField.get(controlView) as? DefaultTimeBar

        if (exoTimeBar != null) {
            (exoTimeBar.parent as? ViewGroup)?.let { parent ->
                val exoTimeBarLayoutParams = exoTimeBar.layoutParams
                timeBar.layoutParams = exoTimeBarLayoutParams
                timeBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    bottomMargin = 0
                }
                val exoTimeBarIndex = parent.indexOfChild(exoTimeBar)
                parent.removeView(exoTimeBar)
                parent.addView(timeBar, exoTimeBarIndex)
            }

            if (timeBar is TimeBar) {
                val onScrubListeners: CopyOnWriteArraySet<OnScrubListener>? =
                    exoTimeBar.getFieldValue<DefaultTimeBar, CopyOnWriteArraySet<OnScrubListener>>("listeners")
                onScrubListeners?.forEach {
                    timeBar.addListener(it)
                }
            }
        }

        timeBarField.set(controlView, timeBar)
        timeBarInManagerField.set(controlViewLayoutManager, timeBar)
    }.onSuccess {
        PurpleLogger.current.d(
            TAG,
            "proxyTimeBar success"
        )
    }.onFailure {
        it.printStackTrace()
        PurpleLogger.current.d(
            TAG,
            "proxyTimeBar exception:${it.message}"
        )
    }
    //
    val translationYForNoBars =
        4f * timeBar.context.resources.getDimension(R.dimen.exo_styled_bottom_bar_height)//timeBar.measuredHeight.toFloat()
    val showAllBarsAnimator: AnimatorSet? =
        controlViewLayoutManager.getFieldValue<Any, AnimatorSet>(
            controlViewLayoutManager.javaClass,
            "showAllBarsAnimator"
        )
    showAllBarsAnimator?.duration = 350
    showAllBarsAnimator?.play(
        ofTranslationY(translationYForNoBars, 0f, timeBar)
    )
    val hideAllBarsAnimator: AnimatorSet? =
        controlViewLayoutManager.getFieldValue<Any, AnimatorSet>(
            controlViewLayoutManager.javaClass,
            "hideAllBarsAnimator"
        )
    hideAllBarsAnimator?.duration = 350
    hideAllBarsAnimator?.play(
        ofTranslationY(0f, translationYForNoBars, timeBar)
    )

    /* val showMainBarAnimator: AnimatorSet? =
         controlViewLayoutManager.getFieldValue<Any, AnimatorSet>(
             controlViewLayoutManager.javaClass,
             "showMainBarAnimator"
         )
     showMainBarAnimator?.duration = 350
     showMainBarAnimator?.play(
         ofTranslationY(translationYForNoBars, 0f, timeBar)
     )
     val hideMainBarAnimator: AnimatorSet? =
         controlViewLayoutManager.getFieldValue<Any, AnimatorSet>(
             controlViewLayoutManager.javaClass,
             "hideMainBarAnimator"
         )
     hideMainBarAnimator?.duration = 350
     hideMainBarAnimator?.play(
         ofTranslationY(0f, translationYForNoBars, timeBar)
     )*/

    val hideProgressBarAnimator: AnimatorSet? =
        controlViewLayoutManager.getFieldValue<Any, AnimatorSet>(
            controlViewLayoutManager.javaClass,
            "hideProgressBarAnimator"
        )
    hideProgressBarAnimator?.duration = 350
    hideProgressBarAnimator?.play(
        ofTranslationY(0f, translationYForNoBars, timeBar)
    )
}

private fun ofTranslationY(startValue: Float, endValue: Float, target: View): ObjectAnimator {
    return ObjectAnimator.ofFloat(target, "translationY", startValue, endValue);
}

