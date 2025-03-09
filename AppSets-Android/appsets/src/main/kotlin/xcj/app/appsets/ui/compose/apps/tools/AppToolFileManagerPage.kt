package xcj.app.appsets.ui.compose.apps.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.compose_share.components.BackActionTopBar

@Composable
fun AppToolFileManagerPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.file_manager)
        )
    }
}