package xcj.app.appsets.util.reflect

import com.rabbitmq.client.Method
import xcj.app.starter.android.util.PurpleLogger
import java.lang.reflect.Field

const val TAG = "FieldReflector"

class ReflectionContext {
    val fieldCacheMap: MutableMap<Class<*>, List<Field>> = mutableMapOf()
    val methodCacheMap: MutableMap<Class<*>, List<Method>> = mutableMapOf()
    fun begin() {
        fieldCacheMap.clear()
        methodCacheMap.clear()
    }

    fun end() {

    }
}

inline fun <reified C> getField(fieldName: String): Field? {
    runCatching {
        val field = C::class.java.getDeclaredField(fieldName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        field
    }.onSuccess {
        return it
    }.onFailure {
        PurpleLogger.current.d(
            TAG,
            "getField:$fieldName for class:${C::class.java}, exception:${it.message}"
        )
    }
    return null
}

inline fun <C> getField(clazz: Class<C>, fieldName: String): Field? {
    runCatching {
        val field = clazz.getDeclaredField(fieldName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        field
    }.onSuccess {
        return it
    }.onFailure {
        PurpleLogger.current.d(
            TAG,
            "getField:$fieldName for class:${clazz}, exception:${it.message}"
        )
    }
    return null
}

inline fun <reified C, O> C.getFieldValue(fieldName: String): O? {
    return getField<C>(fieldName)?.get(this) as? O
}

inline fun <C, O> C.getFieldValue(clazz: Class<C>, fieldName: String): O? {
    return getField<C>(clazz, fieldName)?.get(this) as? O
}