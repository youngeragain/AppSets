package xcj.app.appsets.util.compose_state

interface SingleStateUpdater<S> : ComposeStateUpdater<S> {
    fun getStateValue(): S?

    suspend fun update(state: S?)

    suspend fun update(interval: Long, vararg states: S?)

}