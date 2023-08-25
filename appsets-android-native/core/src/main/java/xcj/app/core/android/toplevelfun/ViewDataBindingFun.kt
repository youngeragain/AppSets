package xcj.app.core.android.toplevelfun

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup

fun <VDB> inflateViewDataBindingFromClass(
    context: Context,
    vdbClazz: Class<VDB>,
    viewGroup: ViewGroup? = null,
): VDB? {
    val layoutInflater = LayoutInflater.from(context)
    return try {
        vdbClazz.getDeclaredMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        ).apply {
            if (!isAccessible) {
                isAccessible = true
            }
        }.invoke(
            null,
            layoutInflater,
            viewGroup,
            false
        ) as? VDB

    } catch (e: Exception) {
        //e.printStackTrace()
        Log.i(
            "ViewDataBindingFun",
            "解析出错!\nContext:${context} 可能的原因:\n1:使用ViewBinding\n2:使用Jetpack Compose\n3:使用传统View系统\n4:使用Kotlin synthetic"
        )
        null
    }
}