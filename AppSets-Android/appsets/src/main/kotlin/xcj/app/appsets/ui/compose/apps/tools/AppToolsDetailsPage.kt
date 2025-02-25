package xcj.app.appsets.ui.compose.apps.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent

@Composable
fun AppToolsDetailsPage(
    type: String?,
    quickStepContents: List<QuickStepContent>?,
    onBackClick: () -> Unit
) {
    HideNavBarWhenOnLaunch()
    Box {
        when (type) {
            TOOL_TYPE_AppSets_Proxy -> {
                AppToolAppSetsProxyComponent(onBackClick = onBackClick)
            }

            TOOL_TYPE_AppSets_Transform -> {
                AppToolQRCodeComponent(
                    quickStepContents = quickStepContents,
                    onBackClick = onBackClick
                )
            }

            TOOL_TYPE_AppSets_Weather -> {
                AppToolWeatherComponent(onBackClick = onBackClick)
            }
        }
    }
}

@Preview
@Composable
fun AppToolsDetailsPagePreview() {
    AppToolsDetailsPage(TOOL_TYPE_AppSets_Proxy, null, onBackClick = {})
}