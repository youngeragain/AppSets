package xcj.app.rtc.ui.purple_module

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.NaviHostParams

class ComposeEventHandler {
    companion object {
        const val ROUTE_APPSETS_RTC = "AppSets_RTC"
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
    }
}