package xcj.app.starter.foundation

class FinalKeyedProvider<K, T>(
    private val key: Identifiable<K>,
    private val t: T
) : KeyedProvider<K, T> {
    override fun key(): Identifiable<K> {
        return key
    }

    override fun provide(): T {
        return t
    }
}