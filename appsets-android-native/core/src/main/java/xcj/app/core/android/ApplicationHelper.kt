package xcj.app.core.android

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import xcj.app.core.test.AndroidContextFileDir

object ApplicationHelper {
    lateinit var coroutineScope: CoroutineScope
        private set
    lateinit var application: Application
        private set
    private lateinit var databases: MutableMap<String, Any>

    private lateinit var moduleRouters: MutableMap<String, ModuleRouter>
    private lateinit var androidContextFileDir: AndroidContextFileDir

    fun getContextFileDir(): AndroidContextFileDir {
        if (!::androidContextFileDir.isInitialized)
            throw Exception()
        return androidContextFileDir
    }

    fun applicationInit(
        application: Application,
        coroutineScope: CoroutineScope,
        androidContextFileDir: AndroidContextFileDir
    ) {
        if (ApplicationHelper::application.isInitialized)
            return
        Log.e("ApplicationHelper", "aplicationInit")
        this.application = application
        this.coroutineScope = coroutineScope
        this.androidContextFileDir = androidContextFileDir
        databases = mutableMapOf()
        moduleRouters = mutableMapOf()
    }

    fun moduleInit(
        moduleName: String,
        moduleRouter: ModuleRouter
    ){
        if(moduleRouters.containsKey(moduleName))
            return
        moduleRouters[moduleName] = moduleRouter
    }

    fun removeModuleInitialization(moduleName: String){
        if(moduleRouters.containsKey(moduleName))
            moduleRouters.remove(moduleName)
    }

    /**
     * @param databaseKey 模块名
     */
    fun addDataBase(databaseKey: String, database: Any) {
        if (databases.containsKey(databaseKey))
            return
        databases[databaseKey] = database
    }

    /**
     * @param databaseKey 模块名
     */
    fun <DB> getDataBase(databaseKey: String): DB? {
        return if (databases.containsKey(databaseKey)) {
            databases[databaseKey] as? DB
        } else null
    }

    fun clearDataBase(databaseKey: String) {
        return
    }

    fun isModuleInit(moduleName: String): Boolean {
        return moduleRouters.containsKey(moduleName)
    }

}