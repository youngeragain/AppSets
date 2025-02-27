package xcj.app.appsets.ui.compose.apps.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import xcj.app.compose_share.components.BackActionTopBar

@Composable
fun AppToolAppSetsProxyPage(onBackClick: () -> Unit) {
    Column {
        BackActionTopBar(
            onBackClick = onBackClick
        )
    }
}