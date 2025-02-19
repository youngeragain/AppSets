package xcj.app.starter.test

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xcj.app.starter.android.util.PurpleLogger

class DefinitionsCollector {

    companion object {
        private const val TAG = "DefinitionsCollector"
        private const val DEFINITION_ROOT_FOLDER_NAME = "purple_info"
    }

    fun collectDefinitions(
        purpleContext: PurpleContext,
        definitionJsonFileName: String = "definition.json"
    ) {
        PurpleLogger.current.d(
            TAG,
            "collectDefinitions, purpleContext:$purpleContext"
        )
        if (purpleContext is SimplePurpleForAndroidContext)
            doAndroidCollectMethod(purpleContext, definitionJsonFileName)
        else
            doAnotherPlatformCollectMethod()
    }

    private fun doAndroidCollectMethod(
        purpleContext: SimplePurpleForAndroidContext,
        definitionJsonFileName: String
    ) {
        PurpleLogger.current.d(TAG, "doAndroidCollectMethod")
        val assetManager = LocalApplication.current.assets
        val definitionFolderName = DEFINITION_ROOT_FOLDER_NAME
        val gson = Gson()
        val modulesDefinition = assetManager.list(definitionFolderName)
        if (modulesDefinition.isNullOrEmpty()) {
            return
        }
        PurpleLogger.current.d(
            TAG,
            "doAndroidCollectMethod module scopes:\n${modulesDefinition.joinToString("\n") { "${definitionFolderName}/$it" }}"
        )
        modulesDefinition.forEach { folderName ->
            assetManager.open("${definitionFolderName}/${folderName}/${definitionJsonFileName}")
                .bufferedReader().use {
                    PurpleLogger.current.d(
                        TAG,
                        "doAndroidCollectMethod collect scope is:${definitionFolderName}/${folderName}"
                    )
                    val definitionRawText = it.readText()
                    if (definitionRawText.isNotEmpty()) {
                        PurpleLogger.current.d(
                            TAG,
                            "doAndroidCollectMethod scope[${definitionFolderName}/${folderName}] definition RawText is:$definitionRawText"
                        )
                        runCatching {
                            val purpleJsonDefinitions =
                                gson.fromJson(
                                    definitionRawText,
                                    object : TypeToken<Map<String, List<String>>>() {}
                                )
                            purpleJsonDefinitions.forEach { definition ->
                                if (Anything::class.java.canonicalName == definition.key) {
                                    purpleContext.definitionAnythingComponents.addAll(definition.value)
                                    return@forEach
                                }
                                val designDefinitionKClass =
                                    Class.forName(definition.key)
                                val userCustomKClassSet =
                                    definition.value.mapNotNull { className ->
                                        runCatching {
                                            return@mapNotNull Class.forName(className)
                                        }.onFailure { tr ->
                                            PurpleLogger.current.e(
                                                TAG,
                                                "doAndroidCollectMethod Class is not found:" + tr.message
                                            )
                                        }
                                        return@mapNotNull null
                                    }
                                purpleContext.definitionClassMap[designDefinitionKClass] =
                                    mutableSetOf<Class<*>>().apply {
                                        purpleContext.definitionClassMap[designDefinitionKClass]?.let { existClasses ->
                                            addAll(existClasses)
                                        }
                                        addAll(userCustomKClassSet)
                                    }
                            }
                        }.onFailure { throwable ->
                            PurpleLogger.current.d(
                                TAG,
                                "doAndroidCollectMethod, FAILURE, error:${throwable.message}, \n${throwable.stackTraceToString()}"
                            )
                        }
                    } else {
                        PurpleLogger.current.d(
                            TAG,
                            "definitionRawText is nullOrEmpty!!!"
                        )
                    }
                }
        }
    }

    private fun doAnotherPlatformCollectMethod() {
        PurpleLogger.current.d(TAG, "doAnotherPlatformCollectMethod")
    }
}