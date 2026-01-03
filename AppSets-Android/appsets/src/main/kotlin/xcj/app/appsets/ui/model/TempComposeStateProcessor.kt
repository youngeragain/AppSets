package xcj.app.appsets.ui.model

import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.starter.android.util.PurpleLogger

abstract class TempComposeStateProcessor() {

    companion object {
        private const val TAG = "TempComposeStateProcessor"
    }

    private var composeStateUpdaterMap: MutableMap<String, ComposeStateUpdater<*>> = mutableMapOf()

    fun markForKey(key: String, composeStateUpdater: ComposeStateUpdater<*>) {
        PurpleLogger.current.d(
            TAG,
            "markForKey, key:$key, before size:${composeStateUpdaterMap.size}"
        )
        composeStateUpdaterMap[key] = composeStateUpdater
        PurpleLogger.current.d(
            TAG,
            "markForKey, key:$key, after size:${composeStateUpdaterMap.size}"
        )
    }

    fun unmarkForKey(key: String) {
        PurpleLogger.current.d(
            TAG,
            "unmarkForKey, key:$key, before size:${composeStateUpdaterMap.size}"
        )
        if (!composeStateUpdaterMap.containsKey(key)) {
            PurpleLogger.current.d(
                TAG,
                "unmarkForKey, key:$key, is not exist in, return"
            )
            return
        }
        composeStateUpdaterMap.remove(key)
        PurpleLogger.current.d(
            TAG,
            "unmarkForKey, key:$key, after size:${composeStateUpdaterMap.size}"
        )
    }

    suspend fun updateForKey(key: String, input: Any?, autoRemove: Boolean = true) {
        PurpleLogger.current.d(TAG, "updateForKey, key:$key, input:$input, autoRemove:$autoRemove")
        if (!composeStateUpdaterMap.containsKey(key)) {
            return
        }
        val composeStateUpdater = composeStateUpdaterMap[key]
        composeStateUpdater?.input(key, input)
        if (autoRemove) {
            unmarkForKey(key)
        }
    }
}