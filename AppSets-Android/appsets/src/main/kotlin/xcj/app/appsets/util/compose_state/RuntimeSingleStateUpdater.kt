package xcj.app.appsets.util.compose_state

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.delay

open class RuntimeSingleStateUpdater<S>(
    private val composeState: MutableState<S?>?,
    private val handleDSL: (suspend RuntimeSingleStateUpdater<S>.(String, Any?) -> Unit)? = null
) : SingleStateUpdater<S> {

    override fun getStateValue(): S? {
        return composeState?.value
    }

    override suspend fun update(state: S?) {
        composeState?.value = state
    }

    override suspend fun update(interval: Long, vararg states: S?) {
        if (states.size == 1) {
            update(states[0])
            return
        }
        states.forEach { state ->
            update(state)
            delay(interval)
        }
    }

    override suspend fun <I : Any> handle(key: String, input: I?) {
        handleDSL?.invoke(this, key, input)
    }


    class RuntimeSingleStateNonNullUpdater<S>(
        private val composeState: MutableState<S>?,
        private val handleDSL: (suspend RuntimeSingleStateNonNullUpdater<S>.(String, Any?) -> Unit)? = null
    ) : SingleStateUpdater<S> {

        override fun getStateValue(): S? {
            return composeState?.value
        }

        override suspend fun update(state: S?) {
            if (state == null) {
                return
            }
            composeState?.value = state
        }

        override suspend fun update(interval: Long, vararg states: S?) {
            if (states.size == 1) {
                update(states[0])
                return
            }
            states.forEach { state ->
                update(state)
                delay(interval)
            }
        }

        override suspend fun <I : Any> handle(key: String, input: I?) {
            handleDSL?.invoke(this, key, input)
        }

        companion object {
            fun <T> fromState(
                composeState: MutableState<T>,
                inputHandleDSL: suspend RuntimeSingleStateNonNullUpdater<T>.(String, Any?) -> Unit
            ): ComposeStateUpdater<T> {
                return RuntimeSingleStateNonNullUpdater(
                    composeState,
                    inputHandleDSL
                ) as ComposeStateUpdater<T>
            }
        }
    }

    companion object {

        fun <T> fromState(
            composeState: MutableState<T?>?,
            inputHandleDSL: (suspend RuntimeSingleStateUpdater<T>.(String, Any?) -> Unit)? = null
        ): ComposeStateUpdater<T> {
            return RuntimeSingleStateUpdater(composeState, inputHandleDSL) as ComposeStateUpdater<T>
        }

        fun <T> fromNonNullState(
            composeState: MutableState<T>?,
            inputHandleDSL: (suspend RuntimeSingleStateNonNullUpdater<T>.(String, Any?) -> Unit)? = null
        ): ComposeStateUpdater<T> {
            return RuntimeSingleStateNonNullUpdater(
                composeState,
                inputHandleDSL
            ) as ComposeStateUpdater<T>
        }
    }
}