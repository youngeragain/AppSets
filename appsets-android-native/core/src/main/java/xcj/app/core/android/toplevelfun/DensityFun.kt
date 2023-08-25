package xcj.app.core.android.toplevelfun

import xcj.app.core.android.ApplicationHelper

fun dp2px(dp:Float):Float{
    val scale = ApplicationHelper.application.resources.displayMetrics.density
    return (dp*scale+0.5f)
}
fun px2dp(px:Float):Float{
    val scale = ApplicationHelper.application.resources.displayMetrics.density
    return (px/scale+0.5f)
}