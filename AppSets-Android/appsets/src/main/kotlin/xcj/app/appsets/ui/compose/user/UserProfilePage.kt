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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import xcj.app.appsets.ui.model.page_state.UserProfilePageState
import xcj.app.compose_share.components.BackActionTopBar

private const val NONE = "None"
private const val APPLICATION = "Application"
private const val SCREEN = "Screen"
private const val FOLLOWER_FOLLOWED = "Follower/Followed"
private const val MODIFY_PROFILE = "ModifyProfile"

private const val GOODS = "Goods"

@Composable
fun UserProfilePage(
    userProfilePageState: UserProfilePageState,
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
    onSelectUserAvatarClick: (String) -> Unit,
    onModifyProfileConfirmClick: () -> Unit,
) {
    HideNavBar()

    val userInfoUseCase = LocalUseCaseOfUserInfo.current
    DisposableEffect(Unit) {
        onDispose {
            userInfoUseCase.onComposeDispose("page dispose")
        }
    }

    var currentShowContent by remember {
        mutableStateOf(NONE)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        when (userProfilePageState) {
            is UserProfilePageState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is UserProfilePageState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.not_found),
                        modifier = Modifier.align(Alignment.Center)
                    )
                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is UserProfilePageState.LoadSuccess -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    BackActionTopBar(
                        onBackClick = onBackClick
                    )
                    if (currentShowContent != NONE) {
                        BackHandler {
                            if (currentShowContent != NONE) {
                                currentShowContent = NONE
                            }
                        }
                    }
                    UserInfoHeader(
                        modifier = Modifier,
                        userInfo = userProfilePageState.userInfo,
                        userFollowers = userFollowers,
                        userFollowed = userFollowed,
                        isLoginUserFollowedThisUser = isLoginUserFollowedThisUser,
                        onActionClick = { userAction ->
                            when (userAction.type) {
                                UserAction.ACTION_APPLICATION -> {
                                    currentShowContent = APPLICATION
                                }

                                UserAction.ACTION_SCREEN -> {
                                    onLoadMoreScreens(userProfilePageState.userInfo.uid, true)
                                    currentShowContent = SCREEN
                                }

                                UserAction.ACTION_FOLLOW_STATE -> {
                                    currentShowContent = FOLLOWER_FOLLOWED
                                }

                                UserAction.ACTION_UPDATE_INFO -> {
                                    currentShowContent = MODIFY_PROFILE
                                }

                                UserAction.ACTION_FLIP_FOLLOW -> {
                                    onFlipFollowClick(userProfilePageState.userInfo)
                                }

                                UserAction.ACTION_CHAT -> {
                                    onChatClick(userProfilePageState.userInfo)
                                }

                                UserAction.ACTION_ADD_FRIEND -> {
                                    onAddFriendClick(userProfilePageState.userInfo)
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
                AnimatedVisibility(
                    visible = currentShowContent != NONE,
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
                        if (currentShowContent != NONE) {
                            val titleText = if (currentShowContent == MODIFY_PROFILE) {
                                stringResource(xcj.app.appsets.R.string.update_information)
                            } else {
                                stringResource(
                                    xcj.app.appsets.R.string.a_of_b,
                                    userProfilePageState.userInfo.bioName ?: "",
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
                            SCREEN -> {
                                UserScreens(
                                    screens = userScreens,
                                    onBioClick = { bio ->
                                        onBioClick.invoke(bio)
                                        currentShowContent = NONE
                                    },
                                    onLoadMore = {
                                        onLoadMoreScreens(userProfilePageState.userInfo.uid, false)
                                    },
                                    onScreenMediaClick = onScreenMediaClick
                                )
                            }

                            APPLICATION -> {
                                UserApplications(
                                    userApplications = userApplications,
                                    onBioClick = onBioClick
                                )
                            }

                            FOLLOWER_FOLLOWED -> {
                                UserFollowers(
                                    uid = userProfilePageState.userInfo.uid,
                                    userFollowers = userFollowers,
                                    userFollowed = userFollowed,
                                    onBioClick = { userInfo ->
                                        onBioClick.invoke(userInfo)
                                        currentShowContent = NONE
                                    }
                                )
                            }

                            MODIFY_PROFILE -> {
                                ProfileModificationComponent(
                                    userInfo = userProfilePageState.userInfo,
                                    onSelectUserAvatarClick = onSelectUserAvatarClick,
                                    onConfirmClick = onModifyProfileConfirmClick
                                )
                            }
                        }
                    }
                }
                if (currentShowContent != NONE) {
                    DesignDropDownButton(modifier = Modifier.align(Alignment.BottomCenter)) {
                        currentShowContent = NONE
                    }
                }
            }
        }
    }
}