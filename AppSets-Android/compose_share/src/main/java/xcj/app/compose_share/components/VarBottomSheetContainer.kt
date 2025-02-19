package xcj.app.compose_share.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState

private const val TAG = "VarBottomSheetContainer"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VarBottomSheetContainer() {
    val context = LocalContext.current
    val anyStateProvider = LocalAnyStateProvider.current
    val state =
        anyStateProvider.bottomSheetState()
    Box(Modifier.fillMaxSize()) {
        if (state.isShow) {
            val rememberModalBottomSheetState = rememberModalBottomSheetState(true)
            ModalBottomSheet(
                onDismissRequest = state::hideAndRemove,
                sheetState = rememberModalBottomSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline)
                }
            ) {
                state.getContent(context)?.Content()
            }
        }
    }
}