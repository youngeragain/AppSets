package xcj.app.starter.android

import xcj.app.starter.android.util.PurpleLogger

object ModuleHelper {

    private const val TAG = "ModuleHelper"

    val databaseMap: MutableMap<String, Any> = mutableMapOf()

    private val moduleMainMap: MutableMap<String, IPurpleModuleMain> = mutableMapOf()


    fun moduleInit(
        moduleName: String,
        IPurpleModuleMain: IPurpleModuleMain
    ){
        if (moduleMainMap.containsKey(moduleName))
            return
        moduleMainMap[moduleName] = IPurpleModuleMain
    }

    fun removeModuleInitialization(moduleName: String){
        if (moduleMainMap.containsKey(moduleName))
            moduleMainMap.remove(moduleName)
    }

    /**
     * @param databaseKey 模块名
     */
    fun addDataBase(databaseKey: String, database: Any) {
        PurpleLogger.current.d(
            TAG,
            "addDataBase, databaseKey:$databaseKey, database:${database}"
        )
        if (databaseMap.containsKey(databaseKey))
            return
        databaseMap[databaseKey] = database
    }

    /**
     * @param databaseKey 模块名
     */
    fun <DB> getDataBase(databaseKey: String): DB? {
        PurpleLogger.current.d(
            TAG,
            "getDataBase, databaseKey:$databaseKey, databases size:${databaseMap.size}"
        )
        return if (databaseMap.containsKey(databaseKey)) {
            databaseMap[databaseKey] as? DB
        } else null
    }

    fun clearDataBase(databaseKey: String) {
        return
    }

    fun isModuleInit(moduleName: String): Boolean {
        return moduleMainMap.containsKey(moduleName)
    }

}