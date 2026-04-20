package xcj.app.appsets.ui.compose.apps.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.compose_share.components.BackActionTopBar

@Composable
fun ToolGraphicPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    HideNavBar()
    Box(modifier = Modifier.fillMaxSize()) {
        BackActionTopBar(
            onBackClick = onBackClick
        )
    }
}