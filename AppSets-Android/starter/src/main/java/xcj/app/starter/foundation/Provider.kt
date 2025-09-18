package xcj.app.starter.foundation

interface Provider<K, T> {
    fun key(): Identifiable<K>
    fun provide(): T
}

class FinalProvider<K, T>(private val key: Identifiable<K>, private val t: T) : Provider<K, T> {
    override fun key(): Identifiable<K> {
        return key
    }

    override fun provide(): T {
        return t
    }
}