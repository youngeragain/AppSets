package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.BackEventCompat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.ui.compose.LocalNavHostController
import xcj.app.appsets.ui.compose.LocalQuickStepContentHandlerRegistry
import xcj.app.appsets.ui.compose.LocalUseCaseOfActivityLifecycle
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
import xcj.app.appsets.ui.compose.settings.LiteSettingsDialog
import xcj.app.appsets.ui.model.NowSpaceObjectState
import xcj.app.appsets.ui.model.NowSpaceObjectState.NewImMessage
import xcj.app.appsets.ui.model.TabAction
import xcj.app.appsets.ui.model.TabItem
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.SessionState
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.ComposeContainerState
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.components.LocalUseCaseOfComposeDynamic
import xcj.app.compose_share.components.ProgressedComposeContainerState
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.immerseContentState

private const val TAG = "MainPages"

fun NavHostController.navigateWithClearStack(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
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
        LocalUseCaseOfActivityLifecycle provides viewModel.activityLifecycleUseCase,
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
        LocalAnyStateProvider provides viewModel,
        LocalQuickStepContentHandlerRegistry provides quickStepContentHandlerRegistry
    ) {
        Box(
            modifier = Modifier.mainBackgroundHandle()
        ) {
            MainScaffoldContainer(navController = navController)

            ImmerseContentContainer(navController = navController)

            BottomSheetContainer()

        }
    }
}

@Composable
fun ImmerseContentContainer(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val anyStateProvider = LocalAnyStateProvider.current
    val immerseContentState = anyStateProvider.immerseContentState()
    Box(
        Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = immerseContentState.isShow,
            enter = fadeIn(tween()) + scaleIn(initialScale = 1.12f, animationSpec = tween()),
            exit = fadeOut(tween()) + scaleOut(targetScale = 1.12f, animationSpec = tween()),
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
    val anyStateProvider = LocalAnyStateProvider.current
    val localQuickStepContentHandlerRegistry = LocalQuickStepContentHandlerRegistry.current

    LaunchedEffect(key1 = systemUseCase.newVersionState, block = {
        if (systemUseCase.newVersionState.value?.forceUpdate == true) {
            delay(200)
            navigationUseCase.barVisible.value = false
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScaffoldContainer(navController: NavHostController) {
    OnScaffoldLaunch(navController)
    val onTabClick = rememberNavigationBarOnTabClickListener(navController)
    Scaffold(
        modifier = Modifier.mainScaffoldHandle(),
        bottomBar = {
            NavigationBarContainer(
                navController = navController,
                onTabClick = onTabClick
            )
        }
    ) { _ ->
        Column {
            val systemUseCase = LocalUseCaseOfSystem.current
            NewVersionSpace(
                updateCheckResult = systemUseCase.newVersionState.value,
                onDismissClick = {
                    systemUseCase.dismissNewVersionTips()
                }
            )
            NowSpace(navController)
            MainNaviHostPages(navController = navController)
        }
    }
}

@Composable
fun NavigationBarContainer(
    navController: NavHostController,
    onTabClick: (TabItem, TabAction?) -> Unit,
) {
    val context = LocalContext.current
    val navigationUseCase = LocalUseCaseOfNavigation.current
    val qrCodeUseCase = LocalUseCaseOfQRCode.current
    val systemUseCase = LocalUseCaseOfSystem.current
    val anyStateProvider = LocalAnyStateProvider.current
    val enable = systemUseCase.newVersionState.value?.forceUpdate != true
    val inSearchModel = navController.currentDestination?.route == PageRouteNames.SearchPage
    val searchUseCase = LocalUseCaseOfSearch.current
    NavigationBar(
        visible = navigationUseCase.barVisible.value,
        enable = enable,
        inSearchModel = inSearchModel,
        tabItems = navigationUseCase.tabItems.value,
        onTabClick = onTabClick,
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
                LiteSettingsDialog(
                    qrCodeInfo = qrCodeUseCase.generatedQRCodeInfo.value,
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
                        qrCodeUseCase.requestGenerateQRCode()
                    },
                    onToScanQRCodeClick = {
                        navigateToCameraActivity(context, navController)
                    },
                    onQRCodeConfirmClick = {
                        qrCodeUseCase.doConfirm()
                    }
                )
            }
        }
    )
}

@Composable
fun NowSpace(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
        val conversationUseCase = LocalUseCaseOfConversation.current


        val content = nowSpaceContentUseCase.content.value
        val currentSession =
            (conversationUseCase.currentSessionState.value as? SessionState.Normal)?.session
        when (content) {
            is NowSpaceObjectState.NewImMessage -> {

                val messageColorBarVisibility =
                    (navController.currentDestination?.route != PageRouteNames.ConversationDetailsPage ||
                            currentSession?.id != content.session.id)
                if (messageColorBarVisibility) {
                    MessageQuickAccessBar(
                        modifier = Modifier.clickable {
                            nowSpaceContentUseCase.removeContent()
                            when (content.imMessage) {
                                is SystemMessage -> {
                                    conversationUseCase.updateCurrentTab(ConversationUseCase.SYSTEM)
                                    navController.navigate(PageRouteNames.ConversationOverviewPage) {
                                        popUpTo(PageRouteNames.ConversationOverviewPage) {
                                            inclusive = true
                                        }
                                    }
                                }

                                else -> {
                                    conversationUseCase.updateCurrentSessionBySession(content.session)
                                    navController.navigate(PageRouteNames.ConversationDetailsPage) {
                                        popUpTo(PageRouteNames.ConversationDetailsPage) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        },
                        content
                    )
                }
            }

            NowSpaceObjectState.NULL -> {
                Spacer(modifier = Modifier.height(0.dp))
            }
        }
    }
}

@Composable
fun MessageQuickAccessBar(
    modifier: Modifier,
    newImMessage: NewImMessage,
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        AnimatedContent(
            targetState = newImMessage,
            transitionSpec = {
                fadeIn(tween()) togetherWith fadeOut(tween())
            },
            contentAlignment = Alignment.Center,
            label = "message_quick_access_bar_animate",
        ) { targetNewImMessage ->
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
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
                        any = targetNewImMessage.session.imObj.avatarUrl
                    )
                    Text(text = targetNewImMessage.session.imObj.name)
                }
                val context = LocalContext.current
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
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
                                        any = systemContentInterface.avatarUrl,
                                        defaultColor = MaterialTheme.colorScheme.secondaryContainer
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
                                        any = systemContentInterface.avatarUrl,
                                        defaultColor = MaterialTheme.colorScheme.secondaryContainer
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
        DesignHDivider()
    }
}

@Composable
fun Modifier.mainBackgroundHandle() = composed {
    val localAnyStateProvider = LocalAnyStateProvider.current
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
    val anyStateProvider = LocalAnyStateProvider.current
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

@Composable
fun rememberNavigationBarOnTabClickListener(navController: NavController): (TabItem, TabAction?) -> Unit {
    val context = LocalContext.current
    val screenUseCase = LocalUseCaseOfScreen.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val listener: (TabItem, TabAction?) -> Unit = remember {
        { tab, tabAction ->
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
    }
    return listener
}

private fun ComposeContainerState.mapToBackEventState(): BackEventCompat {
    val ps = this as ProgressedComposeContainerState
    return ps.progressState.value as BackEventCompat
}