package xcj.app.web.webserver.base

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.DexClassScanner
import xcj.app.web.webserver.interfaces.Controller
import xcj.app.web.webserver.interfaces.RequestMapping
import xcj.app.web.webserver.netty.HandlerMethod
import xcj.app.web.webserver.netty.JsonContentTransformer
import java.lang.reflect.Constructor

object DefaultControllerCollector {

    private const val TAG = "DefaultControllerCollector"

    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    suspend fun collect(
        context: Context,
        packageName: String?,
        vararg args: Any?
    ): List<HandlerMethod>? {
        PurpleLogger.current.d(TAG, "collect, context:$context, packageName:$packageName")
        if (packageName.isNullOrEmpty()) {
            return null
        }
        val classes = DexClassScanner.collectClassByAnnotation(
            context,
            packageName,
            Controller::class.java
        )
        PurpleLogger.current.d(TAG, "collect, classes:\n${classes?.joinToString()}")
        if (classes.isNullOrEmpty()) {
            return null
        }
        val handlerMethods = classes.mapNotNull { clazz ->
            val constructorToUse: Constructor<out Any> =
                if (clazz.declaredConstructors.isEmpty()) {
                    PurpleLogger.current.d(
                        TAG,
                        "collect, clazz:${clazz.simpleName} constructor is empty"
                    )
                    return@mapNotNull null
                } else if (clazz.declaredConstructors.size == 1) {
                    PurpleLogger.current.d(
                        TAG,
                        "collect, clazz:${clazz.simpleName} constructor count is 1"
                    )
                    clazz.declaredConstructors[0]
                } else {
                    PurpleLogger.current.d(
                        TAG,
                        "collect, clazz:${clazz.simpleName} constructor count > 1, use default"
                    )
                    clazz.getConstructor()
                }
            PurpleLogger.current.d(
                TAG,
                "collect, clazz:${clazz.simpleName} constructorToUse:${constructorToUse}"
            )
            val contextObject = try {
                constructorToUse.isAccessible = true
                if (constructorToUse.parameterCount == 0) {
                    PurpleLogger.current.d(
                        TAG,
                        "collect, clazz:${clazz.simpleName} constructor parameterCount is 0"
                    )
                    constructorToUse.newInstance()
                } else {
                    PurpleLogger.current.d(
                        TAG,
                        "collect, clazz:${clazz.simpleName} constructor parameterCount > 1"
                    )
                    val constructorParamsValueToSet: MutableList<Any?> = mutableListOf()
                    constructorToUse.parameterTypes.forEach { pClazz ->
                        val argValue = args.firstOrNull { arg ->
                            if (arg == null) {
                                false
                            } else {
                                arg::class.java == pClazz
                            }
                        }
                        constructorParamsValueToSet.add(argValue)
                    }
                    if (constructorToUse.parameterCount == constructorParamsValueToSet.size) {
                        constructorToUse.newInstance(*constructorParamsValueToSet.toTypedArray())
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                PurpleLogger.current.d(
                    TAG,
                    "collect, create contextObject failed! ${e.message}"
                )
            }
            PurpleLogger.current.d(
                TAG,
                "collect, create contextObject:$contextObject"
            )
            if (contextObject == null) {
                return@mapNotNull null
            }
            PurpleLogger.current.d(
                TAG,
                "collect, create contextObject:$contextObject, declaredMethods count:${clazz.declaredMethods.size}"
            )
            clazz.declaredMethods.mapNotNull { method ->
                val requestMappingAnnotation =
                    method.declaredAnnotations.firstOrNull { annotation ->
                        annotation.annotationClass.java == RequestMapping::class.java
                    } as? RequestMapping
                if (requestMappingAnnotation == null) {
                    PurpleLogger.current.d(
                        TAG,
                        "collect, create contextObject:$contextObject, method:${method.name} " +
                                "does not have Annotation:RequestMapping, make null for it"
                    )
                    null
                } else {
                    val jsonContentTransformer = JsonContentTransformer()
                    PurpleLogger.current.d(
                        TAG,
                        "collect, create contextObject:$contextObject, method:${method.name} " +
                                "have Annotation:RequestMapping, make HandlerMethod for it, " +
                                "jsonContentTransformer:$jsonContentTransformer, " +
                                "uri:${requestMappingAnnotation.path}, " +
                                "httpMethods:${requestMappingAnnotation.method.joinToString()}"
                    )
                    val handlerMethod = runCatching {
                        HandlerMethod(
                            context = context,
                            methodContextObject = contextObject,
                            method = method,
                            uri = requestMappingAnnotation.path,
                            httpMethods = requestMappingAnnotation.method,
                            jsonTransformer = jsonContentTransformer,
                        )
                    }.onFailure {
                        PurpleLogger.current.d(
                            TAG,
                            "collect, create contextObject:$contextObject, method:${method.name} " +
                                    "have Annotation:RequestMapping, make HandlerMethod for it, failed! ${it.message}"
                        )
                    }.getOrNull()
                    handlerMethod
                }
            }
        }.flatten()
        PurpleLogger.current.d(
            TAG,
            "collect, final, handlerMethods:$handlerMethods"
        )
        return handlerMethods
    }
}