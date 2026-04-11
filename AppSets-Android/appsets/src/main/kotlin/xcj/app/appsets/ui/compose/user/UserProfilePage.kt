@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package xcj.app.appsets.ui.compose.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.main.BLUR_RADIUS_MAX
import xcj.app.appsets.ui.compose.main.DesignNaviHostIf
import xcj.app.appsets.ui.model.UserInfoForModify
import xcj.app.appsets.ui.model.page_state.UserProfilePageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable

private const val CONTENT_NONE = "None"
private const val CONTENT_APPLICATION = "Application"
private const val CONTENT_SCREEN = "Screen"
private const val CONTENT_FOLLOWER_FOLLOWED = "Follower/Followed"
private const val CONTENT_MODIFY_PROFILE = "ModifyProfile"

private const val CONTENT_GOODS = "Goods"

@Composable
fun UserProfilePage(
    userProfilePageUIState: UserProfilePageUIState,
    userApplications: List<Application>,
    userFollowers: List<UserInfo>,
    userFollowed: List<UserInfo>,
    isLoginUserFollowedThisUser: Boolean,
    userScreens: List<ScreenInfo>,
    onBackClick: () -> Unit,
    onAddFriendClick: (UserInfo) -> Unit,
    onFlipFollowClick: (UserInfo) -> Unit,
    onChatClick: (UserInfo) -> Unit,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onLoadMoreScreens: (String, Boolean) -> Unit,
    onSelectUserAvatarClick: (String, ComposeStateUpdater<*>) -> Unit,
    onModifyProfileConfirmClick: (UserInfoForModify) -> Unit,
) {
    HideNavBar()

    val userInfoUseCase = LocalUseCaseOfUserInfo.current
    DisposableEffect(Unit) {
        onDispose {
            userInfoUseCase.onComposeDispose("page dispose")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (userProfilePageUIState) {
            is UserProfilePageUIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )

                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is UserProfilePageUIState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = userProfilePageUIState.tips)
                        )
                        userProfilePageUIState.subTips?.let {
                            Text(
                                text = stringResource(id = it),
                                fontSize = 12.sp
                            )
                        }
                    }
                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is UserProfilePageUIState.LoadFailed -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = userProfilePageUIState.tips)
                        )
                        userProfilePageUIState.subTips?.let {
                            Text(
                                text = stringResource(id = it),
                                fontSize = 12.sp
                            )
                        }

                    }
                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is UserProfilePageUIState.LoadSuccess -> {
                UserProfileContent(
                    userInfo = userProfilePageUIState.userInfo,
                    userApplications = userApplications,
                    userFollowers = userFollowers,
                    userFollowed = userFollowed,
                    isLoginUserFollowedThisUser = isLoginUserFollowedThisUser,
                    userScreens = userScreens,
                    onBackClick = onBackClick,
                    onAddFriendClick = onAddFriendClick,
                    onFlipFollowClick = onFlipFollowClick,
                    onChatClick = onChatClick,
                    onBioClick = onBioClick,
                    onApplicationLongPress = onApplicationLongPress,
                    onScreenMediaClick = onScreenMediaClick,
                    onLoadMoreScreens = onLoadMoreScreens,
                    onSelectUserAvatarClick = onSelectUserAvatarClick,
                    onModifyProfileConfirmClick = onModifyProfileConfirmClick
                )
            }
        }
    }
}

@Composable
fun UserProfileContent(
    userInfo: UserInfo,
    userApplications: List<Application>,
    userFollowers: List<UserInfo>,
    userFollowed: List<UserInfo>,
    isLoginUserFollowedThisUser: Boolean,
    userScreens: List<ScreenInfo>,
    onBackClick: () -> Unit,
    onAddFriendClick: (UserInfo) -> Unit,
    onFlipFollowClick: (UserInfo) -> Unit,
    onChatClick: (UserInfo) -> Unit,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onLoadMoreScreens: (String, Boolean) -> Unit,
    onSelectUserAvatarClick: (String, ComposeStateUpdater<*>) -> Unit,
    onModifyProfileConfirmClick: (UserInfoForModify) -> Unit,
) {
    var currentShowContentRoute by remember {
        mutableStateOf(CONTENT_NONE)
    }
    val hazeState = rememberHazeStateIfAvailable()

    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .hazeSourceIfAvailable(hazeState)
                .graphicsLayer {
                    if (currentShowContentRoute != CONTENT_NONE) {
                        renderEffect =
                            BlurEffect(
                                BLUR_RADIUS_MAX,
                                BLUR_RADIUS_MAX
                            )
                    }
                },
        ) {
            StatusBarWithTopActionBarSpacer()
            UserProfileStarter(
                userInfo = userInfo,
                userFollowers = userFollowers,
                userFollowed = userFollowed,
                isLoginUserFollowedThisUser = isLoginUserFollowedThisUser,
                onActionClick = { userAction ->
                    when (userAction.type) {
                        UserAction.ACTION_APPLICATION -> {
                            currentShowContentRoute = CONTENT_APPLICATION
                        }

                        UserAction.ACTION_SCREEN -> {
                            onLoadMoreScreens(userInfo.uid, true)
                            currentShowContentRoute = CONTENT_SCREEN
                        }

                        UserAction.ACTION_FOLLOW_STATE -> {
                            currentShowContentRoute = CONTENT_FOLLOWER_FOLLOWED
                        }

                        UserAction.ACTION_UPDATE_INFO -> {
                            currentShowContentRoute = CONTENT_MODIFY_PROFILE
                        }

                        UserAction.ACTION_FLIP_FOLLOW -> {
                            onFlipFollowClick(userInfo)
                        }

                        UserAction.ACTION_CHAT -> {
                            onChatClick(userInfo)
                        }

                        UserAction.ACTION_ADD_FRIEND -> {
                            onAddFriendClick(userInfo)
                        }
                    }
                }
            )
        }

        DesignNaviHostIf(
            navController = navController,
            startDestination = currentShowContentRoute
        ) {
            composable(CONTENT_NONE) {
                Box {

                }
            }
            composable(CONTENT_APPLICATION) {
                UserApplications(
                    userApplications = userApplications,
                    onBioClick = onBioClick,
                    onApplicationLongPress = onApplicationLongPress
                )
            }

            composable(CONTENT_SCREEN) {
                UserScreens(
                    screens = userScreens,
                    onBioClick = { bio ->
                        onBioClick.invoke(bio)
                        currentShowContentRoute = CONTENT_NONE
                    },
                    onLoadMore = {
                        onLoadMoreScreens(userInfo.uid, false)
                    },
                    onScreenMediaClick = onScreenMediaClick
                )
            }

            composable(CONTENT_FOLLOWER_FOLLOWED) {
                UserFollowers(
                    uid = userInfo.uid,
                    userFollowers = userFollowers,
                    userFollowed = userFollowed,
                    onBioClick = { userInfo ->
                        onBioClick.invoke(userInfo)
                        currentShowContentRoute = CONTENT_NONE
                    }
                )
            }

            composable(CONTENT_MODIFY_PROFILE) {
                ProfileModification(
                    userInfo = userInfo,
                    onSelectUserAvatarClick = onSelectUserAvatarClick,
                    onConfirmClick = onModifyProfileConfirmClick
                )
            }

            composable(CONTENT_GOODS) {

            }
        }

        BackActionTopBar(
            hazeState = hazeState,
            onBackClick = {
                if (currentShowContentRoute != CONTENT_NONE) {
                    currentShowContentRoute = CONTENT_NONE
                } else {
                    onBackClick()
                }
            }
        )
    }
}