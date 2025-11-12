package xcj.app.starter.foundation

fun interface Provider<T> {
    fun provide(): T
}