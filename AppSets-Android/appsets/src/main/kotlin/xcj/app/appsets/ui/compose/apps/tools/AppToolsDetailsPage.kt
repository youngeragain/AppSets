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
                AppToolAppSetsProxyPage(onBackClick = onBackClick)
            }

            TOOL_TYPE_AppSets_Transform -> {
                AppToolQRCodePage(
                    quickStepContents = quickStepContents,
                    onBackClick = onBackClick
                )
            }

            TOOL_TYPE_AppSets_Weather -> {
                AppToolWeatherPage(onBackClick = onBackClick)
            }

            TOOL_TYPE_AppSets_Intent_Caller -> {
                AppToolIntentCallerPage(
                    quickStepContents = quickStepContents,
                    onBackClick = onBackClick
                )
            }

            TOOL_TYPE_AppSets_File_Manager -> {
                AppToolFileManagerPage(
                    quickStepContents = quickStepContents,
                    onBackClick = onBackClick
                )
            }
            TOOL_TYPE_AppSets_Graphic -> {
                AppToolGraphicPage(
                    quickStepContents = quickStepContents,
                    onBackClick = onBackClick
                )
            }
        }
    }
}

@Preview
@Composable
fun AppToolsDetailsPagePreview() {
    AppToolsDetailsPage(TOOL_TYPE_AppSets_Proxy, null, onBackClick = {})
}