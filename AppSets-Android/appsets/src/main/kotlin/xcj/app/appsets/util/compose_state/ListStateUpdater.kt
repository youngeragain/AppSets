package xcj.app.appsets.util.compose_state

interface ListStateUpdater<S> : ComposeStateUpdater<S> {
    fun getStateList(): List<S>?
    suspend fun add(element: S)
    suspend fun addAll(elements: List<S>)
    suspend fun remove(element: S): S?
    suspend fun removeAll()

}