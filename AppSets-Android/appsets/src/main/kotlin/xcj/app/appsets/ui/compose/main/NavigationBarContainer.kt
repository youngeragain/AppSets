package xcj.app.appsets.ui.compose.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.settings.LiteSettingsSheetContent
import xcj.app.appsets.usecase.AppUpdateState
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
    val anyStateProvider = LocalVisibilityComposeStateProvider.current
    val searchUseCase = LocalUseCaseOfSearch.current
    val screenUseCase = LocalUseCaseOfScreen.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val appUpdateState by systemUseCase.appUpdateState
    val currentRoute by navigationUseCase.currentRouteState
    val coroutineScope = rememberCoroutineScope()
    val inSearchModel by remember {
        derivedStateOf {
            currentRoute == PageRouteNames.SearchPage
        }
    }
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
            anyStateProvider.bottomSheetState().show {
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