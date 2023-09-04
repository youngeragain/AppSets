package xcj.app.compose_share.compose.dynamic

interface StatesHolder : LifecycleOwner {
    /**
     * 初始化时调用
     */
    fun onInit()

    /**
     * reusable() 返回true时,该方法会在复用时调用
     */
    fun onReuse()

    /**
     * 该方法始终会调用
     */
    fun onDestroy()

    /**
     * reusable() 返回true时,该方法会在Compose内部出发DisposeEffect的onDispose时调用
     */
    fun onTempDestroy()

    /**
     * 该状态容器是否可复用
     */
    fun reusable(): Boolean
}

