package xcj.app.starter.foundation

import java.lang.reflect.ParameterizedType

class StaticProvider<T> {

    private var t: T? = null

    val current: T
        get() = t
            ?: throw IllegalStateException(
                "current is null, nothing can provide, t class:${
                    (this::class.java.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(
                        0
                    )?.javaClass?.name
                }!"
            )

    fun provider(provider: () -> T?) {
        t = provider()
    }

    infix fun provide(t: T?) {
        provider { t }
    }

    infix fun provide(provider: () -> T?) {
        provider(provider)
    }
}

fun <T> staticProvider(provider: () -> T?): StaticProvider<T> {
    return StaticProvider<T>().apply {
        provide(provider)
    }
}

fun <T> staticProvider(): StaticProvider<T> {
    return StaticProvider()
}


