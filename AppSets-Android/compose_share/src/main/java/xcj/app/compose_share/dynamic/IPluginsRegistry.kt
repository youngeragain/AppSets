package xcj.app.compose_share.dynamic

interface IPluginsRegistry {
    fun registerByAAR(aarPath: String): Boolean
    fun unRegisterByAAR(aarPath: String): Boolean

    fun registerByClassName(key: String, vararg className: String)
    fun <I : IComposeMethodsAware> registerByClass(key: String, vararg clazz: Class<I>)
}