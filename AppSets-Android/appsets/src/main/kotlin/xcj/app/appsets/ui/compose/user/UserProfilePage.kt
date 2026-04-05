@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package xcj.app.appsets.ui.compose.user

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.DesignDropDownButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.model.UserInfoForModify
import xcj.app.appsets.ui.model.page_state.UserProfilePageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.compose_share.components.BackActionTopBar
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
                var currentShowContent by remember {
                    mutableStateOf(CONTENT_NONE)
                }
                val hazeState = rememberHazeStateIfAvailable()
                val density = LocalDensity.current
                var backActionBarSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                val backActionsHeight by remember {
                    derivedStateOf {
                        with(density) {
                            backActionBarSize.height.toDp()
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    if (currentShowContent != CONTENT_NONE) {
                        BackHandler {
                            if (currentShowContent != CONTENT_NONE) {
                                currentShowContent = CONTENT_NONE
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .hazeSourceIfAvailable(hazeState),
                    ) {
                        Spacer(
                            modifier = Modifier.height(
                                backActionsHeight + 12.dp
                            )
                        )
                        UserInfoHeader(
                            userInfo = userProfilePageUIState.userInfo,
                            userFollowers = userFollowers,
                            userFollowed = userFollowed,
                            isLoginUserFollowedThisUser = isLoginUserFollowedThisUser,
                            onActionClick = { userAction ->
                                when (userAction.type) {
                                    UserAction.ACTION_APPLICATION -> {
                                        currentShowContent = CONTENT_APPLICATION
                                    }

                                    UserAction.ACTION_SCREEN -> {
                                        onLoadMoreScreens(userProfilePageUIState.userInfo.uid, true)
                                        currentShowContent = CONTENT_SCREEN
                                    }

                                    UserAction.ACTION_FOLLOW_STATE -> {
                                        currentShowContent = CONTENT_FOLLOWER_FOLLOWED
                                    }

                                    UserAction.ACTION_UPDATE_INFO -> {
                                        currentShowContent = CONTENT_MODIFY_PROFILE
                                    }

                                    UserAction.ACTION_FLIP_FOLLOW -> {
                                        onFlipFollowClick(userProfilePageUIState.userInfo)
                                    }

                                    UserAction.ACTION_CHAT -> {
                                        onChatClick(userProfilePageUIState.userInfo)
                                    }

                                    UserAction.ACTION_ADD_FRIEND -> {
                                        onAddFriendClick(userProfilePageUIState.userInfo)
                                    }
                                }
                            }
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Center),
                                text = stringResource(xcj.app.appsets.R.string.select_a_display_content),
                                fontSize = 12.sp
                            )
                        }
                    }

                    BackActionTopBar(
                        modifier = Modifier.onPlaced {
                            backActionBarSize = it.size
                        },
                        hazeState = hazeState,
                        onBackClick = onBackClick
                    )
                }
                AnimatedVisibility(
                    visible = currentShowContent != CONTENT_NONE,
                    enter = slideInVertically(
                        tween(350),
                        initialOffsetY = { it / 10 }) + fadeIn(tween(350)),
                    exit = slideOutVertically(
                        tween(),
                        targetOffsetY = { it / 10 }) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .fillMaxSize()
                            .statusBarsPadding()
                    ) {
                        if (currentShowContent != CONTENT_NONE) {
                            val titleText = if (currentShowContent == CONTENT_MODIFY_PROFILE) {
                                stringResource(xcj.app.appsets.R.string.update_information)
                            } else {
                                stringResource(
                                    xcj.app.appsets.R.string.a_of_b,
                                    userProfilePageUIState.userInfo.bioName ?: "",
                                    currentShowContent
                                )
                            }
                            Text(
                                text = titleText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        when (currentShowContent) {
                            CONTENT_SCREEN -> {
                                UserScreens(
                                    screens = userScreens,
                                    onBioClick = { bio ->
                                        onBioClick.invoke(bio)
                                        currentShowContent = CONTENT_NONE
                                    },
                                    onLoadMore = {
                                        onLoadMoreScreens(
                                            userProfilePageUIState.userInfo.uid,
                                            false
                                        )
                                    },
                                    onScreenMediaClick = onScreenMediaClick
                                )
                            }

                            CONTENT_APPLICATION -> {
                                UserApplications(
                                    userApplications = userApplications,
                                    onBioClick = onBioClick
                                )
                            }

                            CONTENT_FOLLOWER_FOLLOWED -> {
                                UserFollowers(
                                    uid = userProfilePageUIState.userInfo.uid,
                                    userFollowers = userFollowers,
                                    userFollowed = userFollowed,
                                    onBioClick = { userInfo ->
                                        onBioClick.invoke(userInfo)
                                        currentShowContent = CONTENT_NONE
                                    }
                                )
                            }

                            CONTENT_MODIFY_PROFILE -> {
                                ProfileModification(
                                    userInfo = userProfilePageUIState.userInfo,
                                    onSelectUserAvatarClick = onSelectUserAvatarClick,
                                    onConfirmClick = onModifyProfileConfirmClick
                                )
                            }
                        }
                    }
                }
                if (currentShowContent != CONTENT_NONE) {
                    DesignDropDownButton(modifier = Modifier.align(Alignment.BottomCenter)) {
                        currentShowContent = CONTENT_NONE
                    }
                }
            }
        }
    }
}