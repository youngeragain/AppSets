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
                }.onSuccess { obj ->
                    purpleContext.definitionInstanceList.add(obj)
                    when (obj) {
                        is PurpleContextListener -> {
                            purpleContext.definitionContextListenerInstanceList.add(obj)
                        }

                        is Aware -> {
                            purpleContext.definitionAwareInstanceList.add(obj)
                        }

                        else -> {
                            purpleContext.definitionAnyInstanceList.add(obj)
                        }
                    }
                }.onFailure { throwable ->
                    PurpleLogger.current.d(
                        TAG,
                        "doInitDefinitions, onFailure:${throwable.message}"
                    )
                }
            }
    }
}