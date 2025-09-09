package xcj.app.proxy.ui.purple_module

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import xcj.app.proxy.ui.compose.vpn.AppSetsVpnMainContent
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.NaviHostParams

class ComposeEventHandler {
    companion object {
        const val ROUTE_APPSETS_PROXY = "AppSets_Proxy"
    }

    fun handleEvent(event: ComposeEvent) {
        when (event.eventType) {
            ComposeEvent.EVENT_NAVI_HOST_FORMED -> {
                val naviHostParams = event.eventParams as? NaviHostParams ?: return
                val controller = naviHostParams.navController as? NavController ?: return
                val navGraphBuilder = naviHostParams.navGraphBuilder as? NavGraphBuilder ?: return
                registerMineComposeComponent(controller, navGraphBuilder)
            }
        }
    }

    fun registerMineComposeComponent(
        navController: NavController,
        navGraphBuilder: NavGraphBuilder
    ) {

        navGraphBuilder.composable(ROUTE_APPSETS_PROXY) {
            AppSetsVpnMainContent(
                onConnectButtonClick = {

                }
            )
        }
    }
}