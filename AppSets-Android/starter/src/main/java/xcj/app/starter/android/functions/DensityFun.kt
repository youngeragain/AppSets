package xcj.app.starter.android.functions

import android.content.res.Resources

fun dp2px(dp: Float): Float {
    val scale = Resources.getSystem().displayMetrics.density
    return (dp * scale + 0.5f)
}

fun px2dp(px: Float): Float {
    val scale = Resources.getSystem().displayMetrics.density
    return (px / scale + 0.5f)
}