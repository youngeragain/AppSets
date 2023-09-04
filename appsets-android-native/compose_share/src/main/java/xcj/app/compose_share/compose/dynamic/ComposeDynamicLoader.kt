package xcj.app.compose_share.compose.dynamic

import androidx.compose.runtime.Composable

/**
 * 解析实现IComposeMethods接口的类
 * 然后将解析后得到的@Compose标记方法保存到ComposeMethodsHolder提供的容器内部
 */
abstract class ComposeDynamicLoader(
    val composeMethodsAware: ComposeMethodsAware
) {

    open fun loadByAAR(aarPath: String) {

    }

    open fun <I : IComposeMethods> loadByClass(
        methodsContainer: MutableList<Pair<IComposeMethods, @Composable () -> Unit>>,
        aarName: String?, clazz: Class<I>
    ) {

    }

    fun loadByClassName(
        methodsContainer: MutableList<Pair<IComposeMethods, @Composable () -> Unit>>,
        hostKey: String?,
        className: String
    ) {
        kotlin.runCatching {
            val clazz = Class.forName(className) as? Class<IComposeMethods>
            if (clazz != null) {
                loadByClass(methodsContainer, null, clazz)
            }
        }
    }
}

