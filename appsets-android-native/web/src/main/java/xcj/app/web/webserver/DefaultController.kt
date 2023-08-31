package xcj.app.web.webserver

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import xcj.app.web.webserver.interfaces.Controller
import xcj.app.web.webserver.interfaces.RequestMapping
import xcj.app.web.webserver.netty.HandlerMethod
import xcj.app.web.webserver.netty.JavaScriptObjectNotationWrapper
import java.lang.reflect.Constructor

interface DefaultController{


    companion object{
        @RequiresApi(Build.VERSION_CODES.O)
        @JvmStatic
        fun collect(context: Context, packageName:String?, vararg args:Any?):List<HandlerMethod>?{
            return packageName?.let {
                DexClassScanner.collectClassByAnnotation(
                    context.applicationContext,
                    packageName, Controller::class.java)
            }?.mapNotNull {
                val declaredConstructors = it.declaredConstructors
                var constructorToUse:Constructor<out Any>? = null
                val constructorParamsValueToSet:MutableList<Any?> = mutableListOf()
                if(declaredConstructors.size==1){
                    val constructor = declaredConstructors[0]
                    constructorToUse = constructor
                    if(constructor.parameterCount>0){
                        constructor.parameterTypes.forEach {  pClazz->
                            constructorParamsValueToSet.add(args.firstOrNull { arg-> if(arg==null) false else arg::class.java==pClazz })
                        }
                    }
                }else{
                    it.getConstructor()
                }
                //Log.e("DefaultController", "controller:${it.simpleName} declaredConstructors:${declaredConstructors.size}")
                val contextObject = if(constructorToUse?.parameterCount==0){
                    constructorToUse.newInstance()
                }else{
                    constructorToUse?.newInstance(*constructorParamsValueToSet.toTypedArray())
                }
                if(constructorToUse!=null){
                    it.declaredMethods.mapNotNull { objMethod ->
                        val declaredAnnotations = objMethod.declaredAnnotations
                        declaredAnnotations.firstOrNull { methodAnnotation ->
                            methodAnnotation.annotationClass.java == RequestMapping::class.java
                        }?.let {
                            HandlerMethod().apply {
                                methodContextObject = contextObject!!
                                method = objMethod
                                methodArgumentsTypes = objMethod.parameterTypes
                                methodArgumentsAnnotations = objMethod.parameterAnnotations
                                jsonWrapper = JavaScriptObjectNotationWrapper()
                                returnType = objMethod.returnType
                                val requestMapping = it as RequestMapping
                                uri = requestMapping.path
                                if(uri.contains('{')&&uri.contains('}')){
                                    uriSplitResults = UriSplitResults.slice(uri)
                                }
                                acceptRequestMethods = requestMapping.httpMethod
                            }
                        }
                    }
                }else{
                    null
                }
            }?.flatten()
        }
    }
}