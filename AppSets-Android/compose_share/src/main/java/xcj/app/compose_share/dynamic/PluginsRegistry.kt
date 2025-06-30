package xcj.app.compose_share.dynamic

import com.google.gson.Gson
import dalvik.system.DexClassLoader
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.compose_share.purple_module.MySharedPreferences
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.DexClassScanner
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

object PluginsRegistry : IPluginsRegistry {
    private const val TAG = "PluginsRegistry"
    suspend fun loadMethodsToContainer(composeMethodsAware: ComposeMethodsAware) {
        withContext(
            Dispatchers.IO +
                    CoroutineExceptionHandler { _, throwable ->
                        throwable.printStackTrace()
                        PurpleLogger.current.d(
                            TAG,
                            "exception occur from coroutineScope, throwable:${throwable.message}"
                        )
                    }
        ) {
            PurpleLogger.current.d(TAG, "loadMethodsToContainer")
            val methodsContainer: MutableList<ComposeMethodsWrapper> =
                mutableListOf()

            val loadAllAAR = loadAllAAR<IComposeMethodsAware>()
            if (loadAllAAR.isNullOrEmpty()) {
                composeMethodsAware.setMethodsContainer(methodsContainer)
                return@withContext
            }
            val defaultComposeDynamicLoader = DefaultComposeDynamicLoader(composeMethodsAware)
            loadAllAAR.forEach { (aarName, clazzList) ->
                clazzList?.forEach { iComposeMethod ->
                    defaultComposeDynamicLoader.loadByClass(
                        methodsContainer,
                        aarName,
                        iComposeMethod
                    )
                }
            }
        }
    }

    private fun <I : IComposeMethodsAware> loadAllAAR(): Map<String, List<Class<I>>?>? {
        val keysJson = MySharedPreferences.getString("dynamic_compose_keys")
        if (keysJson.isNullOrEmpty()) {
            return null
        }
        val aarClazzListMap = mutableMapOf<String, List<Class<I>>>()
        val gson = Gson()
        val dynamicComposeKeys = gson.fromJson(keysJson, DynamicComposeKeys::class.java)
        dynamicComposeKeys.keys.mapNotNull { key ->
            val value = MySharedPreferences.getString("dynamic_compose_${key}")
            if (value.isNullOrEmpty()) {
                return@mapNotNull null
            } else {
                value
            }
        }.forEach {
            val dynamicComposeKeyObject = gson.fromJson(it, DynamicComposeKeyObject::class.java)
            if (!dynamicComposeKeyObject.aarPath.isNullOrEmpty()) {
                val file = File(dynamicComposeKeyObject.aarPath!!)
                if (file.exists()) {
                    val clazzList = mutableListOf<Class<I>>()

                    val dexClassLoader = DexClassLoader(
                        dynamicComposeKeyObject.aarPath,
                        LocalAndroidContextFileDir.current.dynamicAAROPTDir,
                        null,
                        javaClass.classLoader
                    )
                    dynamicComposeKeyObject.classNameList?.forEach { iComposeMethodClassName ->
                        runCatching {
                            clazzList.add(dexClassLoader.loadClass(iComposeMethodClassName) as Class<I>)
                        }.onFailure {
                            PurpleLogger.current.d(TAG, "loadAllAAR, exception:${it.message}")
                        }
                    }
                    if (clazzList.isNotEmpty()) {
                        aarClazzListMap[file.nameWithoutExtension] = clazzList
                    }
                }
            }
        }
        return aarClazzListMap
    }

    fun registerAARFromExternal(filePath: String): Boolean {
        val file = File(filePath)
        PurpleLogger.current.d(
            TAG,
            "registerAARFromExternal, file.exist:${file.exists()}"
        )
        if (!file.exists()) {
            return false
        }
        val dynamicAARDir = LocalAndroidContextFileDir.current.dynamicAARDir
        if (dynamicAARDir.isNullOrEmpty()) {
            return false
        }
        val dir = File(dynamicAARDir)
        if (!dir.exists() || !dir.isDirectory) {
            return false
        }
        runCatching {
            val result = registerByAAR(file.path)
            if (result) {
                PurpleLogger.current.d(TAG, "loadAARFromExternal success!")
            } else {
                PurpleLogger.current.d(TAG, "loadAARFromExternal failed!")
                //file.delete()
            }
            return result
        }.onFailure {
            it.printStackTrace()
            PurpleLogger.current.d(
                TAG,
                "loadAARFromExternal failed! exception " + it.message
            )
        }
        return false
    }

    override fun registerByAAR(aarPath: String): Boolean {
        val file = File(aarPath)
        if (!file.exists()) {
            PurpleLogger.current.d(
                TAG,
                "registerByAAR, early return because the aar file not exist!"
            )
            return false
        }
        val key = file.nameWithoutExtension
        val keyObjectJson = MySharedPreferences.getString("dynamic_compose_${key}")
        if (!keyObjectJson.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "registerByAAR, early return because this has same aar file!"
            )
            return false
        }
        val dexClassLoader =
            DexClassLoader(
                aarPath,
                LocalAndroidContextFileDir.current.dynamicAAROPTDir,
                null,
                javaClass.classLoader
            )
        runCatching {
            DexClassScanner.setDexClassLoader(dexClassLoader)
            val allIComposeMethods =
                DexClassScanner.getAllClassByInterface(
                    IComposeMethodsAware::class.java,
                    "xcj.app.compose_share"
                )?.filterNot {
                    it == AbstractComposeMethodsAware::class.java
                }
            DexClassScanner.setDexClassLoader(null)
            allIComposeMethods
        }.onFailure {
            it.printStackTrace()
            PurpleLogger.current.d(
                TAG,
                "Load IComposeMethod failed!" + it.message
            )
            return false
        }.onSuccess { iComposeMethodsClazzList ->
            if (iComposeMethodsClazzList.isNullOrEmpty()) {
                PurpleLogger.current.d(
                    TAG,
                    "registerByAAR, early return because iComposeMethodsClazzList isNullOrEmpty!"
                )
                return false
            }
            PurpleLogger.current.d(
                TAG,
                "Load IComposeMethod success! size of methods:${iComposeMethodsClazzList.size}"
            )
            val gson = Gson()
            if (keyObjectJson.isNullOrEmpty()) {
                val dynamicComposeKeyObject = DynamicComposeKeyObject(
                    aarPath,
                    iComposeMethodsClazzList.mapNotNull { clazz -> clazz.canonicalName }
                        .toMutableList()
                )
                val json = gson.toJson(dynamicComposeKeyObject)
                MySharedPreferences.putString("dynamic_compose_${key}", json)

            } else {
                val dynamicComposeKeyObject =
                    gson.fromJson(keyObjectJson, DynamicComposeKeyObject::class.java)
                dynamicComposeKeyObject.aarPath
                val json = gson.toJson(dynamicComposeKeyObject)
                MySharedPreferences.putString("dynamic_compose_${key}", json)
            }
            val keysJson = MySharedPreferences.getString("dynamic_compose_keys")
            if (keysJson.isNullOrEmpty()) {
                val dynamicComposeKeys = DynamicComposeKeys(mutableListOf(key))
                val json = gson.toJson(dynamicComposeKeys)
                MySharedPreferences.putString("dynamic_compose_keys", json)
            } else {
                val dynamicComposeKeys = gson.fromJson(keysJson, DynamicComposeKeys::class.java)
                if (!dynamicComposeKeys.keys.contains(key)) {
                    dynamicComposeKeys.keys.add(key)
                    val json = gson.toJson(dynamicComposeKeys)
                    MySharedPreferences.putString("dynamic_compose_keys", json)
                }
            }
            return true
        }
        return false
    }

    fun unRegisterByAARByUser(
        aarName: String,
        iComposeMethods: IComposeMethodsAware? = null,
        onFinish: (() -> Unit)? = null
    ) {
        runCatching {
            val keysJson = MySharedPreferences.getString("dynamic_compose_keys")
            val gson = Gson()
            var shouldRemoveKey = false
            val keyObjectJson = MySharedPreferences.getString("dynamic_compose_${aarName}")
            if (!keyObjectJson.isNullOrEmpty()) {
                val dynamicComposeKeyObject =
                    gson.fromJson(keyObjectJson, DynamicComposeKeyObject::class.java)
                if (iComposeMethods == null) {
                    if (!dynamicComposeKeyObject.aarPath.isNullOrEmpty()) {
                        val aarFile = File(dynamicComposeKeyObject.aarPath!!)
                        if (aarFile.exists())
                            aarFile.delete()
                    }
                    MySharedPreferences.remove("dynamic_compose_${aarName}")
                    shouldRemoveKey = true
                } else {
                    dynamicComposeKeyObject.classNameList?.removeIf { it == iComposeMethods::class.java.canonicalName }
                    if (dynamicComposeKeyObject.classNameList.isNullOrEmpty()) {
                        if (!dynamicComposeKeyObject.aarPath.isNullOrEmpty()) {
                            val aarFile = File(dynamicComposeKeyObject.aarPath!!)
                            if (aarFile.exists())
                                aarFile.delete()
                        }
                        MySharedPreferences.remove("dynamic_compose_${aarName}")
                        shouldRemoveKey = true
                    } else {
                        val json = gson.toJson(dynamicComposeKeyObject)
                        MySharedPreferences.putString("dynamic_compose_${aarName}", json)
                    }
                }
                if (shouldRemoveKey) {
                    MySharedPreferences.remove("dynamic_compose_keys")
                    if (!keysJson.isNullOrEmpty()) {
                        val dynamicComposeKeys =
                            gson.fromJson(keysJson, DynamicComposeKeys::class.java)
                        dynamicComposeKeys.keys.removeIf { it == aarName }
                        val json = gson.toJson(dynamicComposeKeys)
                        MySharedPreferences.putString("dynamic_compose_keys", json)
                    }
                }
            }
        }.onSuccess {
            PurpleLogger.current.d(
                TAG,
                "unRegisterByAARByUser success: iComposeMethods:${iComposeMethods}"
            )
            onFinish?.invoke()
        }
    }

    override fun unRegisterByAAR(aarPath: String): Boolean {
        return false
    }

    override fun registerByClassName(key: String, vararg className: String) {

    }

    override fun <I : IComposeMethodsAware> registerByClass(key: String, vararg clazz: Class<I>) {

    }
}