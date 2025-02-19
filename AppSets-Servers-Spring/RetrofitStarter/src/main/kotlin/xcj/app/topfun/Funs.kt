package xcj.app.topfun

import xcj.app.annotation.ApplicationName
import xcj.app.annotation.BaseUrl
import xcj.app.annotation.Interceptors
import xcj.app.singleton.ApplicationProvider
import xcj.app.singleton.RetrofitProvider

fun <T> getApi(clazz: Class<T>):T{
    var baseUrl: String = "https://null/"
    try {
        val declaredApplicationNameAnnotation = clazz.getDeclaredAnnotation(ApplicationName::class.java)
        val declaredBaseUrlAnnotation = clazz.getDeclaredAnnotation(BaseUrl::class.java)
        val declaredInterceptorsAnnotation = clazz.getDeclaredAnnotation(Interceptors::class.java)
        if (declaredApplicationNameAnnotation != null) {
            val applicationName = declaredApplicationNameAnnotation.name
            findIpAddressAndPortFromRegistrationCenterByApplicationName(applicationName)?.let {
                baseUrl = it
            }
        } else if (declaredBaseUrlAnnotation != null) {
            baseUrl = declaredBaseUrlAnnotation.url
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return RetrofitProvider.getService(baseUrl, clazz)
}
/**
 * 扩展Retrofit，通过给接口类添加一个注解@ApplicationName
 * @see ApplicationName
 */
inline fun <reified T> getApi(): T {
    val apiClazz = T::class.java
    var baseUrl: String = "https://null/"
    try {
        val declaredApplicationNameAnnotation = apiClazz.getDeclaredAnnotation(ApplicationName::class.java)
        val declaredBaseUrlAnnotation = apiClazz.getDeclaredAnnotation(BaseUrl::class.java)
        val declaredInterceptorsAnnotation = apiClazz.getDeclaredAnnotation(Interceptors::class.java)
        if (declaredApplicationNameAnnotation != null) {
            val applicationName = declaredApplicationNameAnnotation.name
            findIpAddressAndPortFromRegistrationCenterByApplicationName(applicationName)?.let {
                baseUrl = it
            }
        } else if (declaredBaseUrlAnnotation != null) {
            baseUrl = declaredBaseUrlAnnotation.url
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return RetrofitProvider.getService(baseUrl, T::class.java)
}

fun findIpAddressAndPortFromRegistrationCenterByApplicationName(applicationName: String):String? {
    try {
        val instance = ApplicationProvider.servers.filter {
            it.serviceId == applicationName
        }.random()
        if(instance!=null)
            return "http://${instance.host}:${instance.port}/"
    }catch (e:NoSuchElementException){
        e.printStackTrace()
        return null
    }

    return null
}

fun urlHook(newUrl:()->String):()->String{
    return newUrl
}