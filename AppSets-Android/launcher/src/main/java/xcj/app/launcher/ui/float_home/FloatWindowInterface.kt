package xcj.app.launcher.ui.float_home

import android.content.Context

interface FloatWindowInterface {

    fun initWindow(context: Context)

    fun show(withAnimation: Boolean = true)

    fun hide(withAnimation: Boolean = true)
}
