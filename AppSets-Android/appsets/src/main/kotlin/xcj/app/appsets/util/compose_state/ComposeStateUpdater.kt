package xcj.app.appsets.util.compose_state

interface ComposeStateUpdater<out S> {
    suspend fun <I : Any> input(key: String, input: I?)
}