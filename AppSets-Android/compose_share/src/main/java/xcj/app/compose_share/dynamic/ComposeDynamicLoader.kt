package xcj.app.compose_share.dynamic

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
        methodsContainer: MutableList<ComposeMethodsWrapper>,
        aarName: String?, clazz: Class<I>
    ) {

    }

    fun loadByClassName(
        methodsContainer: MutableList<ComposeMethodsWrapper>,
        hostKey: String?,
        className: String
    ) {
        runCatching {
            val clazz = Class.forName(className) as? Class<IComposeMethods>
            if (clazz != null) {
                loadByClass(methodsContainer, null, clazz)
            }
        }
    }
}

