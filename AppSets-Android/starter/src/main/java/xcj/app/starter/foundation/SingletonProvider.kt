package xcj.app.starter.foundation

import xcj.app.starter.android.util.PurpleLogger
import kotlin.reflect.KClass

object SingletonProvider {
    const val TAG = "SingletonProvider"

    val singletonMap = mutableMapOf<KClass<*>, Any>()

    inline fun <reified T> provide(noinline factory: (() -> T)? = null): T? {
        val kClass = T::class
        val get = singletonMap[kClass] as? T
        if (get != null) {
            return get
        }
        synchronized(singletonMap) {
            try {
                val instance = kClass.java.getConstructor().newInstance()
                (instance as? Any)?.let { singletonMap.put(kClass, it) }
                return instance
            } catch (e: Exception) {
                PurpleLogger.current.e(TAG, "provideSingleton")
            }
            val instance = factory?.invoke()
            (instance as? Any)?.let { singletonMap.put(kClass, it) }
            return instance
        }
    }

    inline fun <reified T> remove(): T? {
        return synchronized(singletonMap) {
            singletonMap.remove(T::class)
        } as? T
    }
}