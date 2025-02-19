package xcj.app.util

class StaticProvider<T> {

    private var t: T? = null

    val current: T
        get() = t ?: throw IllegalStateException("current is null, nothing can provide!")

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


