package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
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
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.media.video.fall.MediaFallActivity
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandlerRegistry
import xcj.app.appsets.ui.compose.settings.LiteSettingsSheetContent
import xcj.app.appsets.ui.model.TabAction
import xcj.app.appsets.ui.model.TabItem
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.appsets.ui.model.state.NowSpaceContent.NewImMessage
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.usecase.AppUpdateState
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.ScreenUseCase
import xcj.app.appsets.usecase.SessionState
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.LocalUseCaseOfComposeDynamic
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.components.ProgressiveVisibilityComposeState
import xcj.app.compose_share.components.VisibilityComposeState
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.immerseContentState
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "MainPages"

fun NavHostController.navigateWithClearStack(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

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
fun MainPages() {

    val viewModel = viewModel<MainViewModel>()
    val navController = rememberNavController()
    val quickStepContentHandlerRegistry = remember {
        QuickStepContentHandlerRegistry()
    }
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
        OnScaffoldLaunch(navController)
        MainBox(navController = navController)
    }
}

@Composable
fun MainBox(navController: NavHostController) {
    val hazeState = rememberHazeState()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .mainScaffoldHandle(),
        ) {

            MainNaviHostPages(
                navController = navController,
                startPageRoute = PageRouteNames.AppsCenterPage,
                hazeState = hazeState
            )

            NavigationBarContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                navController = navController,
                hazeState = hazeState
            )

            NowSpace(navController)

            NewVersionSpace()
        }

        ImmerseContentContainer()

        BottomSheetContainer()

    }
}

@Composable
fun ImmerseContentContainer() {
    val context = LocalContext.current
    val anyStateProvider = LocalVisibilityComposeStateProvider.current
    val immerseContentState = anyStateProvider.immerseContentState()
    Box(
        Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = immerseContentState.isShow,
            enter = fadeIn(animationSpec = tween()) + scaleIn(
                initialScale = 1.12f,
                animationSpec = tween()
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 1.12f
            ),
        ) {
            immerseContentState.getContent(context)?.Content()
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

fun handleTabClick(
    context: Context,
    coroutineScope: CoroutineScope,
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
                            coroutineScope.launch {
                                screenUseCase.loadOutSideScreens()
                            }
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

@Composable
fun NavigationBarContainer(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    hazeState: HazeState
) {
    val context = LocalContext.current
    val navigationUseCase = LocalUseCaseOfNavigation.current
    val qrCodeUseCase = LocalUseCaseOfQRCode.current
    val systemUseCase = LocalUseCaseOfSystem.current
    val anyStateProvider = LocalVisibilityComposeStateProvider.current
    val searchUseCase = LocalUseCaseOfSearch.current
    val screenUseCase = LocalUseCaseOfScreen.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val appUpdateState by systemUseCase.appUpdateState
    val coroutineScope = rememberCoroutineScope()
    val inSearchModel by rememberUpdatedState(
        navController.currentDestination?.route == PageRouteNames.SearchPage
    )
    val isBarEnable by remember {
        derivedStateOf {
            (appUpdateState as? AppUpdateState.Checked)
                ?.updateCheckResult
                ?.forceUpdate != true
        }
    }
    val isBarVisible by navigationUseCase.barVisible
    val tabItems by navigationUseCase.tabItems
    NavigationBar(
        modifier = modifier,
        hazeState = hazeState,
        hostVisible = isBarVisible,
        enable = isBarEnable,
        inSearchModel = inSearchModel,
        tabItems = tabItems,
        onTabClick = { tab, tabAction ->
            handleTabClick(
                context,
                coroutineScope,
                tab,
                tabAction,
                navController,
                screenUseCase,
                conversationUseCase
            )
        },
        onSearchBarClick = {
            navController.navigate(PageRouteNames.SearchPage)
        },
        onBackClick = {
            navController.navigateUp()
        },
        onInputContent = {
            searchUseCase.updateKeywords(it)
        },
        onBioClick = {
            anyStateProvider.bottomSheetState().show {
                val generatedQRCodeInfo by qrCodeUseCase.generatedQRCodeInfo
                LiteSettingsSheetContent(
                    qrCodeInfo = generatedQRCodeInfo,
                    onBioClick = {
                        val bio = LocalAccountManager.userInfo
                        onBioClick(context, navController, bio)
                    },
                    onSettingsClick = {
                        navController.navigate(PageRouteNames.SettingsPage)
                    },
                    onSettingsLoginClick = {
                        systemUseCase.loginToggle(context, navController)
                    },
                    onGenQRCodeClick = {
                        coroutineScope.launch {
                            qrCodeUseCase.requestGenerateQRCode()
                        }
                    },
                    onToScanQRCodeClick = {
                        navigateToCameraActivity(context, navController)
                    },
                    onQRCodeConfirmClick = {
                        coroutineScope.launch {
                            qrCodeUseCase.doConfirm()
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun NowSpace(navController: NavController) {
    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val nowSpaceContent by nowSpaceContentUseCase.content
    val currentSessionState by conversationUseCase.currentSessionState

    val messageColorBarVisible by remember {
        derivedStateOf {
            val newImMessage = nowSpaceContent as? NowSpaceContent.NewImMessage
            if (newImMessage == null) {
                false
            } else {
                val currentSession =
                    (currentSessionState as? SessionState.Normal)?.session
                currentSession?.id != newImMessage.session.id ||
                        navController.currentDestination?.route != PageRouteNames.ConversationDetailsPage
            }
        }
    }
    Box(
        modifier = Modifier
    ) {
        AnimatedVisibility(
            visible = messageColorBarVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            if (nowSpaceContent is NowSpaceContent.NewImMessage) {
                val content = nowSpaceContent as NowSpaceContent.NewImMessage
                MessageQuickAccessBar(
                    modifier = Modifier,
                    newImMessage = content,
                    onMessageClick = {
                        when (content.imMessage) {
                            is SystemMessage -> {
                                conversationUseCase.updateCurrentTab(ConversationUseCase.SYSTEM)
                                navController.navigate(PageRouteNames.ConversationOverviewPage) {
                                    popUpTo(PageRouteNames.ConversationOverviewPage) {
                                        inclusive = true
                                    }
                                }
                                nowSpaceContentUseCase.removeContent()
                            }

                            else -> {
                                conversationUseCase.updateCurrentSessionBySession(content.session)
                                navController.navigate(PageRouteNames.ConversationDetailsPage) {
                                    popUpTo(PageRouteNames.ConversationDetailsPage) {
                                        inclusive = true
                                    }
                                }
                                nowSpaceContentUseCase.removeContent()
                            }
                        }
                    }
                )
            }

        }
    }
}

@Composable
fun MessageQuickAccessBar(
    modifier: Modifier,
    newImMessage: NewImMessage,
    onMessageClick: () -> Unit,
) {
    val context = LocalContext.current
    val theNewImMessage by rememberUpdatedState(newImMessage)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.shapes.large
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onMessageClick)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = theNewImMessage,
                label = "message_quick_access_bar_animate_0",
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { targetNewImMessage ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnyImage(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.extraLarge
                            ),
                        model = targetNewImMessage.session.imObj.avatarUrl
                    )
                    Text(text = targetNewImMessage.session.imObj.name)
                }
            }

            AnimatedContent(
                targetState = theNewImMessage,
                label = "message_quick_access_bar_animate_1",
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { targetNewImMessage ->
                Column {
                    if (targetNewImMessage.imMessage is SystemMessage) {
                        val systemContentInterface =
                            targetNewImMessage.imMessage.systemContentInterface
                        when (systemContentInterface) {
                            is FriendRequestJson -> {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnyImage(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(MaterialTheme.shapes.extraLarge)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                MaterialTheme.shapes.extraLarge
                                            ),
                                        model = systemContentInterface.avatarUrl
                                    )
                                    Text(text = systemContentInterface.name ?: "", fontSize = 12.sp)
                                }
                            }

                            is GroupRequestJson -> {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnyImage(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(MaterialTheme.shapes.extraLarge)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline,
                                                MaterialTheme.shapes.extraLarge
                                            ),
                                        model = systemContentInterface.avatarUrl
                                    )
                                    Text(text = systemContentInterface.name ?: "", fontSize = 12.sp)
                                }
                            }

                            else -> Unit
                        }
                    }
                    Text(
                        text = ImMessage.readableContent(context, targetNewImMessage.imMessage)
                            ?: "",
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun Modifier.mainBackgroundHandle() = composed {
    val localAnyStateProvider = LocalVisibilityComposeStateProvider.current
    val bottomSheetState = localAnyStateProvider.bottomSheetState()
    if (!bottomSheetState.shouldBackgroundSink()) {
        return@composed this
    }
    val scale = if (bottomSheetState.isShow) {
        0.9f
    } else {
        1f
    }
    val offsetY = if (bottomSheetState.isShow) {
        (68).dp
    } else {
        0.dp
    }
    val shapeDp = if (bottomSheetState.isShow) {
        32.dp
    } else {
        0.dp
    }
    val shapeState by animateDpAsState(shapeDp, animationSpec = tween())
    val offsetYState by animateDpAsState(offsetY, animationSpec = tween())
    val scaleState by animateFloatAsState(scale, animationSpec = tween())

    val clipShape by remember {
        derivedStateOf {
            RoundedCornerShape(shapeState)
        }
    }

    fillMaxSize()
        .offset(y = offsetYState)
        .scale(scaleState)
        .clip(clipShape)
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

private fun VisibilityComposeState.asBackEventState(): BackEventCompat {
    val ps = this as ProgressiveVisibilityComposeState
    return ps.progressState.value as BackEventCompat
}