package xcj.app.appsets.ui.compose.main

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNowSpaceContent
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.media.video.fall.MediaFallActivity
import xcj.app.appsets.ui.compose.settings.LiteSettingsSheetContent
import xcj.app.appsets.ui.model.TabAction
import xcj.app.appsets.ui.model.TabItem
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.ScreenUseCase
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState

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
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    val searchUseCase = LocalUseCaseOfSearch.current
    val screenUseCase = LocalUseCaseOfScreen.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
    val nowSpaceContents = nowSpaceContentUseCase.contents
    val currentRoute by navigationUseCase.currentRouteState
    val coroutineScope = rememberCoroutineScope()
    val inSearchModel by remember {
        derivedStateOf {
            currentRoute == PageRouteNames.SearchPage
        }
    }
    val isBarEnable by remember {
        derivedStateOf {
            (nowSpaceContents.firstOrNull { it is NowSpaceContent.AppVersionChecked } as? NowSpaceContent.AppVersionChecked)
                ?.updateCheckResult
                ?.forceUpdate != true
        }
    }
    val isBarVisible by navigationUseCase.barVisible
    val tabItems by navigationUseCase.tabItems
    NavigationBar(
        modifier = modifier,
        hazeState = hazeState,
        visible = isBarVisible,
        enable = isBarEnable,
        inSearchModel = inSearchModel,
        tabItems = tabItems,
        onTabClick = { tab, tabAction ->
            coroutineScope.launch {
                handleTabClick(
                    context,
                    tab,
                    tabAction,
                    navController,
                    screenUseCase,
                    conversationUseCase
                )
            }
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
            val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
            bottomSheetState.show {
                val generatedQRCodeInfo by qrCodeUseCase.generatedQRCodeInfo
                LiteSettingsSheetContent(
                    qrCodeInfo = generatedQRCodeInfo,
                    onBioClick = {
                        val bio = LocalAccountManager.userInfo
                        coroutineScope.launch {
                            onBioClick(context, navController, bio)
                        }
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

private suspend fun handleTabClick(
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