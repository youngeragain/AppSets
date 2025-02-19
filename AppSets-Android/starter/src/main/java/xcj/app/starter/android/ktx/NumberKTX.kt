package xcj.app.starter.android.ktx

import xcj.app.starter.android.functions.dp2px

fun Float.dp(): Float {
    return dp2px(this)
}

fun Int.dp(): Int {
    return this.toFloat().dp().toInt()
}