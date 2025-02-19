package xcj.app.launcher.ui.float_home

import android.os.Binder

class FloatWindowHomeBinder(
    private val floatWindowInterface: FloatWindowInterface
) : Binder() {
    fun showWindow() {
        floatWindowInterface.show()
    }

    fun hideWindow() {
        floatWindowInterface.hide()
    }
}