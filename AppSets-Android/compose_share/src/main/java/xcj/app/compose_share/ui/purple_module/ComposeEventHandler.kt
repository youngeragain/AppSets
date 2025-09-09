package xcj.app.compose_share.ui.purple_module

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import xcj.app.compose_share.components.LocalUseCaseOfComposeDynamic
import xcj.app.compose_share.dynamic.DynamicPage
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.NaviHostParams

class ComposeEventHandler {
    companion object {
        const val ROUTE_COMPOSE_DYNAMIC = "Compose_Share"
    }

    fun handleEvent(event: ComposeEvent) {
        when (event.eventType) {
            ComposeEvent.EVENT_NAVI_HOST_FORMED -> {
                val naviHostParams = event.eventParams as? NaviHostParams ?: return
                val controller = naviHostParams.navController as? androidx.navigation.NavController
                    ?: return
                val navGraphBuilder =
                    naviHostParams.navGraphBuilder as? androidx.navigation.NavGraphBuilder
                        ?: return
                registerMineComposeComponent(controller, navGraphBuilder)
            }
        }
    }

    fun registerMineComposeComponent(
        navController: androidx.navigation.NavController,
        navGraphBuilder: androidx.navigation.NavGraphBuilder
    ) {

        navGraphBuilder.composable(ROUTE_COMPOSE_DYNAMIC) {
            val context = LocalContext.current
            val composeDynamicUseCase = LocalUseCaseOfComposeDynamic.current
            val coroutineScope = rememberCoroutineScope()
            DisposableEffect(key1 = true) {
                coroutineScope.launch {
                    composeDynamicUseCase.doLoad()
                }
                onDispose {
                    composeDynamicUseCase.onComposeDispose("page dispose")
                }
            }
            DynamicPage(
                onBackClick = navController::navigateUp,
                onAddClick = {
                    composeDynamicUseCase.onAddClick(context)
                },
                onDeleteClick = {
                    composeDynamicUseCase.onDeleteClick(it)
                },
                composeMethods = composeDynamicUseCase.composeMethodsState,
            )
        }
    }
}