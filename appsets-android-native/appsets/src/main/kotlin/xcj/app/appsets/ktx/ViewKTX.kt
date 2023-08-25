package xcj.app.appsets.ktx

import android.view.View
import android.view.animation.AlphaAnimation

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}


fun View.animateAlpha(duration: Long) {
    val alpha = AlphaAnimation(0f, 1f)
    alpha.duration = duration
    this.startAnimation(alpha)
}