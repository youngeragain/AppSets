package xcj.app.appsets.ktx

import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.core.android.ApplicationHelper
import xcj.app.core.android.DesignMessageDeliver

fun <T : Any, U:Any> U.post(any: T?=null, delayed: Long = 0L) {
    DesignMessageDeliver.post(this, any, delayed)
}

fun <T : Any, U:Any> U.observeAny2(viewLifecycleOwner: LifecycleOwner, observer: Observer<T?>) {
    DesignMessageDeliver.observe(this, viewLifecycleOwner, observer)
}

suspend fun String?.toastSuspend(duration: Int = Toast.LENGTH_SHORT){
    withContext(Dispatchers.Main){
        Toast.makeText(ApplicationHelper.application, this@toastSuspend, duration).show()
    }
}
fun String?.toast(duration: Int = Toast.LENGTH_SHORT){
    Toast.makeText(ApplicationHelper.application, this, duration).show()
}