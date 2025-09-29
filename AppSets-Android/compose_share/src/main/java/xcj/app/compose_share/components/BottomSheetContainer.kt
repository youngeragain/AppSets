package xcj.app.compose_share.components

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState

private const val TAG = "BottomSheetContainer"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContainer(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    val bottomSheetState =
        visibilityComposeStateProvider.bottomSheetState()
    val bottomSheetVisibilityComposeState = bottomSheetState as BottomSheetVisibilityComposeState

    if (bottomSheetState.isShow) {
        val rememberModalBottomSheetState = rememberModalBottomSheetState(true)
        ModalBottomSheet(
            onDismissRequest = {
                bottomSheetState.hideAndRemove()
                bottomSheetVisibilityComposeState.resetIfNeeded()
            },
            sheetState = rememberModalBottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline)
            },
            properties = bottomSheetVisibilityComposeState.getFoundationDesignModalBottomSheetProperties()
        ) {
            bottomSheetState.getContent(context)?.Content()
        }
    }
}