package xcj.app.starter.foundation

class LazyStaticProvider<T> : StaticProvider<T>() {

    private var provider: (() -> T?)? = null

    override val current: T
        get() {
            if (t != null) {
                return t!!
            }
            val theProvider = provider
            if (theProvider != null) {
                t = theProvider.invoke()
                provider = null
                return t!!
            }
            throw IllegalStateException(
                "current is null, nothing can provide!"
            )
        }

    override fun provider(provider: () -> T?) {
        this.provider = provider
    }

    override infix fun provide(t: T?) {
        provider { t }
    }

    override infix fun provide(provider: () -> T?) {
        provider(provider)
    }
}

fun <T> lazyStaticProvider(provider: () -> T?): LazyStaticProvider<T> {
    return LazyStaticProvider<T>().apply {
        provide(provider)
    }
}

fun <T> lazyStaticProvider(): LazyStaticProvider<T> {
    return LazyStaticProvider()
}


