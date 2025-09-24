package xcj.app.appsets.ui.compose.apps.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.compose_share.components.BackActionTopBar

@Composable
fun ToolGraphicPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    HideNavBar()
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.graphic)
        )
    }
}