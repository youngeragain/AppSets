package xcj.app.appsets.util.ktx

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

fun LifecycleOwner.asContextOrNull(): Context? {
    if (this is Activity) {
        return this
    }
    if (this is Fragment) {
        return this.context
    }
    if (this is AppCompatDialog) {
        return this.context
    }
    return null
}