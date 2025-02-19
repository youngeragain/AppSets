package xcj.app.starter.test

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.Aware

class DefinitionsInstantiator {
    companion object {
        private const val TAG = "DefinitionsInstantiator"
    }

    fun doInitDefinitions(purpleContext: PurpleContext) {
        PurpleLogger.current.d(TAG, "doInitDefinitions")
        purpleContext
            .definitionClassMap
            .values
            .flatten()
            .forEach {
                runCatching {
                    val customKClassConstructor = it.getConstructor()
                    customKClassConstructor.newInstance()
                }.onSuccess {
                    purpleContext.definitionInstanceList.add(it)
                    when (it) {
                        is PurpleContextListener -> {
                            purpleContext.definitionContextListenerInstanceList.add(it)
                        }

                        is Aware -> {
                            purpleContext.definitionAwareInstanceList.add(it)
                        }

                        else -> {
                            purpleContext.definitionAnyInstanceList.add(it)
                        }
                    }
                }.onFailure {
                    PurpleLogger.current.d(
                        TAG,
                        "doInitDefinitions, onFailure, message:${it.message}"
                    )
                }
            }
    }
}