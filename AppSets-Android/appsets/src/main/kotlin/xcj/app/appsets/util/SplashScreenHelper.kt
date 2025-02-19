package xcj.app.appsets.util

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Build
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd

object SplashScreenHelper {

    @JvmStatic
    fun onActivityCreate(activity: Activity) {
        splashStyleExposeIcon(activity)
    }

    @JvmStatic
    fun splashStyleExposeIcon(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }
        activity.splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Create your custom animation.
            val scaleAnimator = ObjectAnimator.ofFloat(1f, 1.8f)
            scaleAnimator.interpolator = AccelerateDecelerateInterpolator()
            scaleAnimator.duration = 100L
            scaleAnimator.addUpdateListener { animator ->
                (animator.animatedValue as? Float)?.let { fl ->
                    splashScreenView.scaleX = fl
                    splashScreenView.scaleY = fl
                }
                splashScreenView.alpha = 1 - animator.animatedFraction
            }
            // Call SplashScreenView.remove at the end of your custom animation.
            scaleAnimator.doOnEnd {
                splashScreenView.remove()
                activity.splashScreen.clearOnExitAnimationListener()
            }
            // Run your animation.
            scaleAnimator.start()
        }
    }
}