package xcj.app.launcher.ui.purple_module

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.NaviHostParams

class ComposeEventHandler {
    companion object {
        const val ROUTE_LAUNCHER_STARTER = "Launcher_Starter"
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

        navGraphBuilder.composable(ROUTE_LAUNCHER_STARTER) {
            Box(modifier = Modifier.fillMaxSize()) {
                FilledTonalButton(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = {
                        navController.navigateUp()
                    }
                ) {
                    Text("Launcher Route")
                }
            }
        }
    }
}