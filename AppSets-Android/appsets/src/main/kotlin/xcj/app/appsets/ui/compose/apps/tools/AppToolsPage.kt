package xcj.app.appsets.ui.compose.apps.tools

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.main.navigateToAppSetsLauncherActivity
import xcj.app.appsets.ui.compose.main.navigateToAppSetsShareActivity
import xcj.app.appsets.ui.compose.main.navigateToAppSetsVpnActivity
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignHDivider

const val TOOL_TYPE = "tool_type"

const val TOOL_TYPE_AppSets_Compose_plugin = "AppSets_Compose_Plugin"
const val TOOL_TYPE_AppSets_Transform = "AppSets_Transform"
const val TOOL_TYPE_AppSets_Weather = "AppSets_Weather"
const val TOOL_TYPE_AppSets_Share = "AppSets_Share"
const val TOOL_TYPE_AppSets_Proxy = "AppSets_Proxy"
const val TOOL_TYPE_AppSets_Launcher = "AppSets_Launcher"
const val TOOL_TYPE_AppSets_Intent_Caller = "AppSets_Intent_Caller"
const val TOOL_TYPE_AppSets_File_Manager = "AppSets_File_Manager"
const val TOOL_TYPE_AppSets_Graphic = "AppSets_Graphic"

const val TOOL_TYPE_AppSets_Video = "AppSets_Video"

/**
 * @param routeType activity_navigation_on_host, activity_on_host, activity_navigation_on_lib, activity_on_lib, activity_on_external_app
 */
data class AppTool(
    val icon: Int,
    val name: Int,
    val description: Int,
    val type: String,
    val toolCategory: String? = null,
    val routeType: String = "activity_navigation_on_host",//"host", "lib"
    val routeBuilder: ((Context, NavController) -> Any?)? = null,
)

@Composable
fun generateAppToolList(): List<AppTool> {
    val tools = remember {
        listOf(
            AppTool(
                xcj.app.compose_share.R.drawable.ic_round_swap_calls_24,
                xcj.app.appsets.R.string.appsets_share,
                xcj.app.appsets.R.string.appsets_share_description,
                TOOL_TYPE_AppSets_Share,
                routeType = "activity_on_host",
                routeBuilder = { context, navController ->
                    navigateToAppSetsShareActivity(context, null)
                }
            ),
            AppTool(
                xcj.app.compose_share.R.drawable.ic_outline_language_24,
                xcj.app.appsets.R.string.appsets_proxy,
                xcj.app.appsets.R.string.appsets_proxy_description,
                TOOL_TYPE_AppSets_Proxy,
                routeType = "activity_navigation_on_host",
                routeBuilder = { context, navController ->
                    navigateToAppSetsVpnActivity(context)
                }
            ),


            AppTool(
                xcj.app.compose_share.R.drawable.ic_home_24,
                xcj.app.appsets.R.string.appsets_launcher,
                xcj.app.appsets.R.string.appsets_launcher_description,
                TOOL_TYPE_AppSets_Launcher,
                routeType = "activity_on_lib",
                routeBuilder = { context, navController ->
                    navigateToAppSetsLauncherActivity(context)
                }
            ),

            AppTool(
                xcj.app.compose_share.R.drawable.ic_extension_24,
                xcj.app.compose_share.R.string.compose_plugin,
                xcj.app.compose_share.R.string.compose_plugin_description,
                TOOL_TYPE_AppSets_Compose_plugin,
                routeType = "activity_navigation_on_lib",
                routeBuilder = { context, navController ->
                    navController.navigate(xcj.app.compose_share.ui.purple_module.ComposeEventHandler.ROUTE_COMPOSE_DYNAMIC)
                }
            ),

            AppTool(
                xcj.app.compose_share.R.drawable.ic_outline_qr_code_24,
                xcj.app.appsets.R.string.transform_content,
                xcj.app.appsets.R.string.transform_content_description,
                TOOL_TYPE_AppSets_Transform,
                routeType = "activity_navigation_on_host",
                routeBuilder = null
            ),

            AppTool(
                xcj.app.compose_share.R.drawable.ic_call_made_24,
                xcj.app.appsets.R.string.intent_caller,
                xcj.app.appsets.R.string.intent_caller_description,
                TOOL_TYPE_AppSets_Intent_Caller,
                routeType = "activity_navigation_on_host",
                routeBuilder = null
            ),

            AppTool(
                xcj.app.compose_share.R.drawable.ic_folder_24,
                xcj.app.appsets.R.string.file_manager,
                xcj.app.appsets.R.string.file_manager_description,
                TOOL_TYPE_AppSets_File_Manager,
                routeType = "activity_navigation_on_host",
                routeBuilder = null
            ),

            AppTool(
                xcj.app.compose_share.R.drawable.ic_square_foot_24,
                xcj.app.appsets.R.string.graphic,
                xcj.app.appsets.R.string.graphic_description,
                TOOL_TYPE_AppSets_Graphic,
                routeType = "activity_navigation_on_host",
                routeBuilder = null
            ),

            AppTool(
                xcj.app.compose_share.R.drawable.ic_cloud_24,
                xcj.app.appsets.R.string.weather,
                xcj.app.appsets.R.string.weather_description,
                TOOL_TYPE_AppSets_Weather,
                routeType = "activity_navigation_on_host",
                routeBuilder = null
            ),
            AppTool(
                xcj.app.compose_share.R.drawable.ic_camera_24,
                xcj.app.appsets.R.string.camera,
                xcj.app.appsets.R.string.weather_description,
                TOOL_TYPE_AppSets_Video,
                routeType = "activity_on_lib",
                routeBuilder = { context, controller -> null },
            ),
        )
    }
    return tools
}

@Composable
fun AppToolsPage(
    onBackClick: () -> Unit,
    onToolClick: (AppTool) -> Unit
) {
    HideNavBarWhenOnLaunch()
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(id = xcj.app.appsets.R.string.tools)
        )
        val tools = generateAppToolList()
        LazyColumn {
            items(tools) { appTool ->
                Column {
                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToolClick(appTool)
                            }
                            .padding(16.dp)
                    ) {
                        Image(
                            painter = painterResource(id = appTool.icon),
                            contentDescription = stringResource(id = appTool.name)
                        )
                        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                text = stringResource(id = appTool.name)
                            )
                            Text(
                                text = stringResource(id = appTool.description),
                                fontSize = 12.sp
                            )
                        }

                    }
                    DesignHDivider()
                }
            }
        }
    }
}


@Preview
@Composable
fun AppToolsPagePreview() {
    AppToolsPage(onBackClick = {}, onToolClick = {})
}
