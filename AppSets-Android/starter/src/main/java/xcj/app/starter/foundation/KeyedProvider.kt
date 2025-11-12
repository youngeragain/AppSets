package xcj.app.starter.foundation

interface KeyedProvider<K, T> : Provider<T> {
    fun key(): Identifiable<K>
}

