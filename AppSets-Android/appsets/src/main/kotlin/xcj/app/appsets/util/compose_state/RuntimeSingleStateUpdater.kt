package xcj.app.appsets.util.compose_state

import androidx.compose.runtime.MutableState
import kotlinx.coroutines.delay

open class RuntimeSingleStateUpdater<S>(
    private val composeState: MutableState<S?>?,
    private val onInputHandleDSL: (suspend RuntimeSingleStateUpdater<S>.(String, Any?) -> Unit)? = null
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

    override suspend fun <I : Any> input(key: String, input: I?) {
        onInputHandleDSL?.invoke(this, key, input)
    }


    class RuntimeSingleStateNonNullUpdater<S>(
        private val composeState: MutableState<S>?,
        private val onInputHandleDSL: (suspend RuntimeSingleStateNonNullUpdater<S>.(String, Any?) -> Unit)? = null
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

        override suspend fun <I : Any> input(key: String, input: I?) {
            onInputHandleDSL?.invoke(this, key, input)
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

        /**
         * @param onInputHandleDSL param1:key of composeState, param2:input raw value
         */
        fun <S> fromState(
            composeState: MutableState<S?>?,
            onInputHandleDSL: (suspend RuntimeSingleStateUpdater<S>.(String, Any?) -> Unit)? = null
        ): ComposeStateUpdater<S> {
            return RuntimeSingleStateUpdater(composeState, onInputHandleDSL)
        }

        /**
         * @param onInputHandleDSL param1:key of ComposeState, param2:input raw value
         */
        fun <S> fromNonNullState(
            composeState: MutableState<S>?,
            onInputHandleDSL: (suspend RuntimeSingleStateNonNullUpdater<S>.(String, Any?) -> Unit)? = null
        ): ComposeStateUpdater<S> {
            return RuntimeSingleStateNonNullUpdater(
                composeState,
                onInputHandleDSL
            )
        }
    }
}