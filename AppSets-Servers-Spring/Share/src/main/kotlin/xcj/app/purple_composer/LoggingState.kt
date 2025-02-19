package xcj.app.purple_composer

class LoggingState(val tag: String) {
    val messages: MutableList<Any?> = mutableListOf()
    var throwable: Throwable? = null

    fun clear() {
        messages.clear()
        throwable = null
    }
}