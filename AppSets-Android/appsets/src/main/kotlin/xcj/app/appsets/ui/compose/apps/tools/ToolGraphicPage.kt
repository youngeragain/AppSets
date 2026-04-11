package xcj.app.appsets.ui.compose.apps.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable

@Composable
fun ToolGraphicPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    HideNavBar()
    val hazeState = rememberHazeStateIfAvailable()
    Box(modifier = Modifier.fillMaxSize()) {
        BackActionTopBar(
            hazeState = hazeState,
            onBackClick = onBackClick
        )
    }
}