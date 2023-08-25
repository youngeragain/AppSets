package xcj.app.core.android.ktx

import xcj.app.core.android.toplevelfun.dp2px

fun Float.dp():Float {
    return dp2px(this)
}

fun Int.dp():Int {
    return this.toFloat().dp().toInt()
}