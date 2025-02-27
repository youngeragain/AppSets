package xcj.app.compose_share.dynamic

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import xcj.app.starter.android.util.PurpleLogger

class DefaultComposeDynamicLoader(composeMethodsAware: ComposeMethodsAware) :
    ComposeDynamicLoader(composeMethodsAware) {

    companion object {
        private const val TAG = "DefaultComposeDynamicLoader"
    }

    override fun <I : IComposeMethods> loadByClass(
        methodsContainer: MutableList<ComposeMethodsWrapper>,
        aarName: String?, clazz: Class<I>
    ) {
        //TODO 添加使用注解的解析逻辑
        if (!AbstractComposeMethods::class.java.isAssignableFrom(clazz)) {
            return
        }
        try {
            PurpleLogger.current.d(
                TAG,
                "loadByClass, aarName:${aarName}, clazz:${clazz}, constructors:${clazz.constructors.size}"
            )
            val composeMethodInstance = clazz.getConstructor().newInstance()
            //添加其他逻辑以便在不创建类的情况下提前比对版本
            for (wrapper in methodsContainer) {
                if (wrapper.iComposeMethods.javaClass == clazz) {
                    val oldVersionMetadata = wrapper.iComposeMethods.getVersionMetadata()
                    val stateHolder = wrapper.iComposeMethods.getStatesHolder()
                    if (composeMethodInstance.getVersionMetadata().newerThan(oldVersionMetadata)) {
                        wrapper.iComposeMethods.onComposeDispose("by load class newer version metadata")
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
            val compose: @Composable () -> Unit = {
                AndroidView(
                    factory = { context ->
                        val view = runCatching {
                            declaredMethod.invoke(composeMethodInstance, context) as? View
                        }.onFailure {
                            PurpleLogger.current.d(
                                TAG,
                                "IComposeMethods method(:content) invoke failed:" + it.message
                            )
                        }.getOrNull() ?: run {
                            TextView(context).apply {
                                text = "compose parse error"
                            }
                        }
                        view
                    }
                )
            }
            val composeMethodsWrapper = ComposeMethodsWrapper(composeMethodInstance, compose)
            methodsContainer.add(composeMethodsWrapper)
            composeMethodsAware.setMethodsContainer(methodsContainer)
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.d(TAG, "loadByClass parse error:" + e.message)
        }
    }
}