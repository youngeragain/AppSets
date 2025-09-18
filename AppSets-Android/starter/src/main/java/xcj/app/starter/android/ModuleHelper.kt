package xcj.app.starter.android

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.Provider

object ModuleHelper {

    private const val TAG = "ModuleHelper"

    private val providers: MutableMap<String, Provider<String, *>> = mutableMapOf()

    fun moduleInitHooks(
        iPurpleModule: IPurpleModule
    ) {
        iPurpleModule.initModule()
    }

    /**
     * @param provider
     */
    fun addProvider(provider: Provider<String, *>) {
        PurpleLogger.current.d(
            TAG, "addProvider, providerKey:${provider.key()}"
        )

        val key = provider.key().id
        if (providers.containsKey(key)) {
            return
        }
        providers[key] = provider
    }

    /**
     * @param key 模块名
     */
    fun <T> get(key: String): T? {
        PurpleLogger.current.d(
            TAG, "get, key:$key, providers size:${providers.size}"
        )
        if (!providers.containsKey(key)) {
            return null
        }
        return providers[key]?.provide() as? T
    }

    fun removeProvider(key: String) {
        providers.remove(key)
    }

    fun removeAllProviders() {
        providers.clear()
    }

}