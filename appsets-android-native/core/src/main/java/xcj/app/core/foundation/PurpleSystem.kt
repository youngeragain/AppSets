package xcj.app.core.foundation

interface PurpleSystem {}

interface PurplePluginsSystem:PurpleSystem {
    fun addPlugin(plugin: Plugin<PurpleSystem>)
    fun removePluginByName(name:String):Int
    fun removePluginsByType(clazz: Class<Plugin<PurpleSystem>>):Int
    fun removeAllPlugins():Int
    fun disablePluginByName(name:String):Boolean
    fun enablePluginByName(name:String):Boolean
}