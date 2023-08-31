package xcj.app.core.test

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xcj.app.core.android.TestLogger

class DefinitionsCollector{
    fun collectDefinitions(
        purpleContext: PurpleContext,
        definitionJsonFileName: String = "definitions.json"
    ) {
        if (purpleContext is SimplePurpleForAndroidContext)
            doAndroidCollectMethod(purpleContext, definitionJsonFileName)
        else
            doAnotherPlatformCollectMethod()
    }

    private fun doAndroidCollectMethod(
        purpleContext: SimplePurpleForAndroidContext,
        definitionJsonFileName: String
    ) {
        val assetManager = purpleContext.androidContexts.application.assets
        val definitionFolderName = "definition"
        val gson = Gson()
        assetManager.list(definitionFolderName)?.forEach { folderName ->
            assetManager.open("${definitionFolderName}/${folderName}/${definitionJsonFileName}")
                .bufferedReader().use {
                    val definitionRawText = it.readText()
                    if (definitionRawText.isNotEmpty()) {
                        val purpleJsonDefinitions =
                            gson.fromJson(
                                definitionRawText,
                                object : TypeToken<List<PurpleJsonDefinition>>() {})
                        purpleJsonDefinitions.forEach { definition ->
                            TestLogger.log(definition)
                            kotlin.runCatching {
                                val designDefinitionKClass =
                                    Class.forName(definition.designDefinitionKClass)
                                val userCustomKClassList =
                                    definition.customKClassList.mapNotNull { className ->
                                        kotlin.runCatching {
                                            Class.forName(className)
                                        }.onSuccess { clazz ->
                                            return@mapNotNull clazz
                                        }.onFailure {
                                            return@mapNotNull null
                                        }
                                        return@mapNotNull null
                                    }.toMutableList()
                                if (purpleContext.definitionClassMap.containsKey(
                                        designDefinitionKClass
                                    )
                                )
                                    purpleContext.definitionClassMap[designDefinitionKClass]?.addAll(
                                        userCustomKClassList
                                    )
                                else
                                    purpleContext.definitionClassMap[designDefinitionKClass] =
                                        userCustomKClassList
                            }
                        }
                    }
                }
        }

        TestLogger.log(purpleContext.definitionClassMap)
    }

    private fun doAnotherPlatformCollectMethod() {

    }
}