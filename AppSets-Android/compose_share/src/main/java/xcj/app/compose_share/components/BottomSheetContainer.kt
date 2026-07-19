package xcj.app.compose_share.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import xcj.app.compose_share.modifier.hazeEffectIfAvailableWithTag
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState

private const val TAG = "BottomSheetContainer"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun BottomSheetContainer(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    val bottomSheetState =
        visibilityComposeStateProvider.bottomSheetState()
    val bottomSheetVisibilityComposeState = bottomSheetState as BottomSheetVisibilityComposeState
    val hazeState = LocalHazedStateMap.current[HAZE_KEY_OF_MAIN]
    if (bottomSheetState.isShow) {
        val rememberModalBottomSheetState = rememberModalBottomSheetState(true)
        ModalBottomSheet(
            onDismissRequest = {
                bottomSheetState.hideAndRemove()
                bottomSheetVisibilityComposeState.resetIfNeeded()
            },
            sheetState = rememberModalBottomSheetState,
            containerColor = Color.Transparent,
            dragHandle = null,
            properties = bottomSheetVisibilityComposeState.getFoundationDesignModalBottomSheetProperties()
        ) {
            Box(
                modifier = Modifier
                    .hazeEffectIfAvailableWithTag(TAG, hazeState, HazeMaterials.thin())
            ) {
                bottomSheetState.getContent(context)?.Content()
            }
        }
    }
}