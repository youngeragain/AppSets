package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.LocalNavHostController
import xcj.app.appsets.ui.compose.LocalQuickStepContentHandlerRegistry
import xcj.app.appsets.ui.compose.LocalUseCaseOfAppCreation
import xcj.app.appsets.ui.compose.LocalUseCaseOfApps
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfGroupInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaAudioRecorder
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNowSpaceContent
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreenPost
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.media.video.fall.MediaFallActivity
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandlerRegistry
import xcj.app.appsets.ui.model.TabAction
import xcj.app.appsets.ui.model.TabItem
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.usecase.AppUpdateState
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.ScreenUseCase
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.LocalUseCaseOfComposeDynamic
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.immerseContentState
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "MainPages"

@SuppressLint("RestrictedApi")
fun NavHostController.navigateWithBundle(
    route: String,
    bundleCreator: () -> Bundle,
) {
    val destinationId = findDestination(route)?.id
    if (destinationId == null) {
        PurpleLogger.current.d(
            TAG,
            "navigateWithBundle, route:$route, destinationId is null, return"
        )
        return
    }
    val navDirections: NavDirections = object : NavDirections {
        override val actionId: Int = destinationId
        override val arguments: Bundle = bundleCreator()
    }
    navigate(navDirections)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainPage() {

    val viewModel = viewModel<MainViewModel>()
    val navController = rememberNavController()
    val quickStepContentHandlerRegistry = remember {
        QuickStepContentHandlerRegistry()
    }
    val hazeState = rememberHazeState()

    CompositionLocalProvider(
        LocalUseCaseOfNavigation provides viewModel.navigationUseCase,
        LocalUseCaseOfComposeDynamic provides viewModel.composeDynamicUseCase,
        LocalUseCaseOfSystem provides viewModel.systemUseCase,
        LocalUseCaseOfScreenPost provides viewModel.screenPostUseCase,
        LocalUseCaseOfSearch provides viewModel.searchUseCase,
        LocalUseCaseOfQRCode provides viewModel.qrCodeUseCase,
        LocalUseCaseOfAppCreation provides viewModel.appCreationUseCase,
        LocalUseCaseOfGroupInfo provides viewModel.groupInfoUseCase,
        LocalUseCaseOfScreen provides viewModel.screensUseCase,
        LocalUseCaseOfMediaRemoteExo provides viewModel.mediaRemoteExoUseCase,
        LocalUseCaseOfMediaAudioRecorder provides viewModel.mediaAudioRecorderUseCase,
        LocalUseCaseOfConversation provides viewModel.conversationUseCase,
        LocalUseCaseOfApps provides viewModel.appsUseCase,
        LocalUseCaseOfUserInfo provides viewModel.userInfoUseCase,
        LocalUseCaseOfNowSpaceContent provides viewModel.nowSpaceContentUseCase,
        LocalNavHostController provides navController,
        LocalVisibilityComposeStateProvider provides viewModel,
        LocalQuickStepContentHandlerRegistry provides quickStepContentHandlerRegistry
    ) {
        Surface {
            OnScaffoldLaunch(navController)
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .mainScaffoldHandle(),
                ) {

                    MainNaviHostPagesContainer(
                        navController = navController,
                        startPageRoute = PageRouteNames.AppsCenterPage,
                        hazeState = hazeState,
                        hostContextName = MainActivity.TAG
                    )

                    NavigationBarContainer(
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                        navController = navController,
                        hazeState = hazeState
                    )
                }

                ImmerseContentContainer()

                BottomSheetContainer()

                NowSpaceContainer(navController = navController)

                NewVersionSpaceContainer()

            }
        }
    }
}

@Composable
fun OnScaffoldLaunch(navController: NavController) {
    val context = LocalContext.current
    val systemUseCase = LocalUseCaseOfSystem.current
    val navigationUseCase = LocalUseCaseOfNavigation.current
    val anyStateProvider = LocalVisibilityComposeStateProvider.current
    val localQuickStepContentHandlerRegistry = LocalQuickStepContentHandlerRegistry.current
    val appUpdateState by systemUseCase.appUpdateState
    LaunchedEffect(Unit) {
        anyStateProvider.bottomSheetState().markComposeAvailableState(true)
    }
    LaunchedEffect(key1 = appUpdateState, block = {
        val updateState = appUpdateState
        if (updateState is AppUpdateState.Checked) {
            if (updateState.updateCheckResult.forceUpdate == true) {
                navigationUseCase.barVisible.value = false
            }
        }
    })

    DisposableEffect(key1 = Unit, effect = {
        navigationUseCase.initTabItems()
        val destinationChangedListener: NavController.OnDestinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                anyStateProvider.bottomSheetState().hide()
                navigationUseCase.invalidateTabItemsOnRouteChanged(
                    destination.route,
                    "On Destination Changed"
                )
            }
        navController.addOnDestinationChangedListener(destinationChangedListener)

        QuickStepContentHandlerRegistry.initHandlers(context, localQuickStepContentHandlerRegistry)

        onDispose {
            navController.removeOnDestinationChangedListener(destinationChangedListener)
            QuickStepContentHandlerRegistry.deInitHandlers(localQuickStepContentHandlerRegistry)
        }
    })
}

@Composable
fun Modifier.mainScaffoldHandle(): Modifier = composed {
    val anyStateProvider = LocalVisibilityComposeStateProvider.current
    val immerseContentState = anyStateProvider.immerseContentState()
    val renderEffectAnimateState = remember {
        AnimationState(0f)
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(immerseContentState.isShow) {
        coroutineScope.launch {
            val target = if (immerseContentState.isShow) {
                30f
            } else {
                0f
            }
            renderEffectAnimateState.animateTo(target)
        }
    }
    graphicsLayer {
        if (immerseContentState.isShow) {
            renderEffect =
                BlurEffect(
                    renderEffectAnimateState.value,
                    renderEffectAnimateState.value
                )
        }
    }
}

fun NavHostController.navigateWithClearStack(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

suspend fun handleTabClick(
    context: Context,
    tab: TabItem,
    tabAction: TabAction?,
    navController: NavController,
    screenUseCase: ScreenUseCase,
    conversationUseCase: ConversationUseCase
) {
    if (tabAction != null) {
        when (tab.routeName) {
            PageRouteNames.AppsCenterPage -> {
                if (tabAction.route.isNullOrEmpty()) {
                    when (tabAction.action) {
                        TabAction.ACTION_APP_TOOLS -> {

                        }
                    }
                } else {
                    tabAction.route?.let(navController::navigate)
                }
            }

            PageRouteNames.OutSidePage -> {
                if (tabAction.route.isNullOrEmpty()) {
                    when (tabAction.action) {
                        TabAction.ACTION_REFRESH -> {
                            screenUseCase.loadOutSideScreens()
                        }
                    }
                } else {
                    when (tabAction.route) {
                        PageRouteNames.MediaFallPage -> {
                            context.startActivity(
                                Intent(
                                    context,
                                    MediaFallActivity::class.java
                                )
                            )
                        }

                        else -> {
                            tabAction.route?.let(navController::navigate)
                        }
                    }
                }
            }

            PageRouteNames.ConversationOverviewPage -> {
                conversationUseCase.toggleShowAddActions()
            }
        }
    } else if (!tab.isSelect) {
        navController.navigate(tab.routeName, navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        })
    }
}