package xcj.app.starter.foundation

import java.lang.reflect.ParameterizedType

class LazyStaticProvider<T> {

    private var provider: (() -> T?)? = null
    private var t: T? = null

    val current: T
        get() {
            if (t != null) {
                return t!!
            }
            if (provider != null) {
                t = provider?.invoke()
                return t!!
            }
            throw IllegalStateException(
                "current is null, nothing can provide, t class:${
                    (this::class.java.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.get(
                        0
                    )?.javaClass?.name
                }!"
            )
        }

    fun provider(provider: () -> T?) {
        this.provider = provider
    }

    infix fun provide(t: T?) {
        provider { t }
    }

    infix fun provide(provider: () -> T?) {
        provider(provider)
    }
}

fun <T> lazyStaticProvider(provider: () -> T?): StaticProvider<T> {
    return StaticProvider<T>().apply {
        provide(provider)
    }
}

fun <T> lazyStaticProvider(): StaticProvider<T> {
    return StaticProvider()
}


