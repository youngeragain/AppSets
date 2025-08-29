package xcj.app.starter.foundation

open class StaticProvider<T> {

    protected var t: T? = null

    open val current: T
        get() = t
            ?: throw IllegalStateException(
                "current is null, nothing can provide!"
            )

    open fun provider(provider: () -> T?) {
        t = provider()
    }

    open infix fun provide(t: T?) {
        provider { t }
    }

    open infix fun provide(provider: () -> T?) {
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


