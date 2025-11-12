package xcj.app.appsets.util.compose_state

interface ComposeStateUpdater<S> {
    suspend fun <I : Any> handle(key: String, input: I?)
}