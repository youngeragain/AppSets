package xcj.app.appsets.util.compose_state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList

sealed interface ComposeState<T> {
    data class SingleState<S>(val state: MutableState<S>) : ComposeState<S>
    data class ListState<S>(val state: SnapshotStateList<S>) : ComposeState<S>
}