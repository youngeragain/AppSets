package xcj.app.core.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * 背景：
 * 如果只是调用模块提供的功能，但是由于该功能需要依赖于模块内部的一些初始化信息
 *
 * 该类的作用就是处理这种情况，在模块的主要入口Activity实现ModuleMainEntry接口，
 * 接口中的initModule方法适时完成moduleRouter中的逻辑以便统一初始化模块的逻辑
 */
interface ModuleRouter {
    fun <T:AppCompatActivity> toPage(
        context: Context,
        toActivityClazz: Class<T>,
        launchFlag:Int? = null,
        extra:Bundle? = null
    ){
        context.startActivity(Intent(context, toActivityClazz).apply {
            if (launchFlag != null)
                flags = launchFlag
            putExtra("extra", extra)
        })
    }
}