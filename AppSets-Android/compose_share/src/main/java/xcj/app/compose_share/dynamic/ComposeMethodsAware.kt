package xcj.app.compose_share.dynamic

/**
 * 一个保存解析后的Compose标记的方法的容器提供者，用于在占位的Compose方法内部调用
 */
interface ComposeMethodsAware {
    fun setMethodsContainer(methods: MutableList<ComposeMethodsWrapper>)
}