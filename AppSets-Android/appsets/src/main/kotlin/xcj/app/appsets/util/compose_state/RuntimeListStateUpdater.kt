package xcj.app.appsets.util.compose_state

open class RuntimeListStateUpdater<S>(
    private val composeState: MutableList<S>?,
    private val onInputHandleDSL: (suspend RuntimeListStateUpdater<S>.(String, Any?) -> Unit)? = null
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
        val removed = composeState?.remove(element) ?: false
        return if (removed) {
            element
        } else {
            null
        }
    }

    override suspend fun removeAll() {
        composeState?.clear()
    }

    override suspend fun <I : Any> input(key: String, input: I?) {
        onInputHandleDSL?.invoke(this, key, input)
    }

    companion object {
        /**
         * @param onInputHandleDSL param1:key of ComposeState, param2:input raw value
         */
        fun <S> fromState(
            composeState: MutableList<S>?,
            onInputHandleDSL: (suspend RuntimeListStateUpdater<S>.(String, Any?) -> Unit)? = null
        ): ComposeStateUpdater<S> {
            return RuntimeListStateUpdater(composeState, onInputHandleDSL)
        }
    }
}