package xcj.app.core.foundation

import kotlin.reflect.KClass


val singletonMap = mutableMapOf<KClass<*>, Any>()

inline fun <reified T> provideSingleton():T? {
    return synchronized(singletonMap){
        try {
            T::class.java.getConstructor().newInstance()
        }catch (e:Exception){
            e.printStackTrace()
            null
        }
    }
}

inline fun <reified T> removeSingleton():T? {
    return synchronized(singletonMap){
        singletonMap.remove(T::class)
    } as? T
}