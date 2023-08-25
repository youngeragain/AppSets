package xcj.app.core.test

import xcj.app.core.foundation.Aware

class DefinitionsInstantiator{
    fun doInitDefiniations(purpleContext: PurpleContext){
        purpleContext.definitionClassMap.values.flatten().forEach {
            kotlin.runCatching {
                val customKClassConstructor = it.getConstructor()
                customKClassConstructor.newInstance()
            }.onSuccess {
                purpleContext.definitionInstanceList.add(it)
                when (it) {
                    is PurpleContextListener -> {
                        purpleContext.definitionContextListenerInstanceList.add(it)
                    }
                    is Aware ->
                        purpleContext.definitionAwareInstanceList.add(it)
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}