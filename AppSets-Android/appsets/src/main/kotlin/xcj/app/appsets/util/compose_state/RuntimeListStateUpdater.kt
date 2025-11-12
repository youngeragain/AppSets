package xcj.app.appsets.util.compose_state

open class RuntimeListStateUpdater<S>(
    private val composeState: MutableList<S>?,
    private val handleDSL: (suspend RuntimeListStateUpdater<S>.(String, Any?) -> Unit)? = null
) : ListStateUpdater<S> {

    override fun getStateList(): List<S>? {
        return composeState?.toList()
    }

    override suspend fun add(element: S) {
        composeState?.add(element)
    }

    override suspend fun addAll(elements: List<S>) {
        composeState?.addAll(elements)
    }


    override suspend fun remove(element: S): S? {
        return composeState?.remove(element) as? S
    }

    override suspend fun removeAll() {
        composeState?.clear()
    }

    override suspend fun <I : Any> handle(key: String, input: I?) {
        handleDSL?.invoke(this, key, input)
    }

    companion object {
        fun <S> fromState(
            composeState: MutableList<S>?,
            inputHandleDSL: (suspend RuntimeListStateUpdater<S>.(String, Any?) -> Unit)? = null
        ): ComposeStateUpdater<S> {
            return RuntimeListStateUpdater(composeState, inputHandleDSL) as ComposeStateUpdater<S>
        }
    }
}