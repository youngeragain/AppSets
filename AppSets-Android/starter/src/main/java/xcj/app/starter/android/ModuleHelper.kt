package xcj.app.starter.android

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.Provider

object ModuleHelper {

    private const val TAG = "ModuleHelper"

    private val providers: MutableMap<String, Provider<String, *>> = mutableMapOf()

    fun moduleInitHooks(
        iPurpleModule: IPurpleModule,
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
     * @param identifiable 模块identifiable
     */
    fun <T> get(identifiable: Identifiable<String>): T? {
        PurpleLogger.current.d(
            TAG, "get, identifiable:${identifiable.id}, providers size:${providers.size}"
        )
        if (!providers.containsKey(identifiable.id)) {
            return null
        }
        return providers[identifiable.id]?.provide() as? T
    }

    fun removeProvider(key: String) {
        providers.remove(key)
    }

    fun removeAllProviders() {
        providers.clear()
    }

}