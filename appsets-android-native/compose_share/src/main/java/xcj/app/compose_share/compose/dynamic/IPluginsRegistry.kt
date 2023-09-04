package xcj.app.compose_share.compose.dynamic

interface IPluginsRegistry {
    fun registerByAAR(aarPath: String): Boolean
    fun unRegisterByAAR(aarPath: String): Boolean

    fun registerByClassName(key: String, vararg className: String)
    fun <I : IComposeMethods> registerByClass(key: String, vararg clazz: Class<I>)
}