package xcj.app.compose_share.compose.dynamic

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import java.lang.reflect.InvocationTargetException

class DefaultComposeDynamicLoader(composeMethodsAware: ComposeMethodsAware) :
    ComposeDynamicLoader(composeMethodsAware) {

    private val TAG = "DefaultComposeDynamicLoader"

    override fun <I : IComposeMethods> loadByClass(
        methodsContainer: MutableList<Pair<IComposeMethods, @Composable () -> Unit>>,
        aarName: String?, clazz: Class<I>
    ) {
        //TODO 添加使用注解的解析逻辑
        if (!AbsComposeMethods::class.java.isAssignableFrom(clazz))
            return
        try {
            Log.e(
                TAG,
                "loadByClass, aarName:${aarName}, clazz:${clazz}, constructors:${clazz.constructors.size}"
            )
            val composeMethodInstance = clazz.getConstructor().newInstance()
            //添加其他逻辑以便在不创建类的情况下提前比对版本
            for (h in methodsContainer) {
                if (h.first.javaClass == clazz) {
                    val oldVersionMetadata = h.first.getVersionMetadata()
                    val stateHolder = h.first.getStatesHolder()
                    if (composeMethodInstance.getVersionMetadata().newerThan(oldVersionMetadata)) {
                        h.first.onComposeDispose("by load class newer version metadata")
                        break
                    } else {
                        if (stateHolder.reusable()) {
                            stateHolder.onReuse()
                            return
                        }
                    }
                }
            }
            composeMethodInstance.setAARName(aarName)
            composeMethodInstance.setLoader(this)
            composeMethodInstance.getStatesHolder().onInit()
            val declaredMethod = clazz.getMethod(
                "content",
                Context::class.java
            )
            try {
                /*Log.e(
                    TAG,
                    "loadByClass content method return type:${declaredMethod.returnType.canonicalName}!"
                )*/
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "loadByClass content method return type get occur exception!"
                )
            }

            /*if (declaredMethod.returnType.name != ComposeView::class.java.name) {
                Log.e(TAG, "loadByClass content method return type not ComposeView!")
                return
            }*/
            val compose: @Composable () -> Unit = {
                AndroidView(factory = { context ->
                    val view = try {
                        declaredMethod.invoke(composeMethodInstance, context) as View
                    } catch (e: InvocationTargetException) {
                        Log.e(
                            TAG,
                            "IComposeMethods method(:content) invoke failed:" + e.targetException.printStackTrace()
                        )
                        TextView(context).apply {
                            text = "compose parse error"
                        }
                    }
                    view
                })
            }
            methodsContainer.add(composeMethodInstance to compose)
            composeMethodsAware.setMethodsContainer(methodsContainer)
        } catch (e: Exception) {
            Log.e(TAG, "loadByClass parse error:" + e.printStackTrace())
        }
    }
}