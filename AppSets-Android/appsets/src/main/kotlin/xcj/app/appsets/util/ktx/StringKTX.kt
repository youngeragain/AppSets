package xcj.app.appsets.util.ktx

import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.test.LocalApplication

suspend fun String?.toastSuspend(duration: Int = Toast.LENGTH_SHORT) {
    if (this.isNullOrEmpty()) {
        return
    }
    withContext(Dispatchers.Main) {
        Toast.makeText(LocalApplication.current, this@toastSuspend, duration).show()
    }
}

fun String?.toast(duration: Int = Toast.LENGTH_SHORT) {
    if (this.isNullOrEmpty()) {
        return
    }
    Toast.makeText(LocalApplication.current, this, duration).show()
}