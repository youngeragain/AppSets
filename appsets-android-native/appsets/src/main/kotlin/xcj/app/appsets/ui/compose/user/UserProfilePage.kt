package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenState
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.outside.LoadMoreHandler
import xcj.app.appsets.ui.compose.outside.ScreensList
import xcj.app.appsets.usecase.UserInfoProfileState
import xcj.app.appsets.usecase.UserRelationsCase
import xcj.app.appsets.usecase.models.Application
import xcj.app.compose_share.compose.BackActionTopBar

@UnstableApi
@Composable
fun UserProfilePage(
    tabVisibilityState: MutableState<Boolean>,
    onBackClick: () -> Unit,
    userInfoState: State<UserInfoProfileState>,
    userApplicationsState: State<List<Application>?>,
    userFollowersState: State<List<UserInfo>?>,
    userFollowedState: State<List<UserInfo>?>,
    myFollowedThisUserState: State<Boolean?>,
    userScreensState: State<List<ScreenState>?>,
    onAddFriendClick: (UserInfo) -> Unit,
    onFollowStateClick: (UserInfo) -> Unit,
    onTalkToUserClick: (UserInfo) -> Unit,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?,
    onLoadMoreScreens: (() -> Unit)?,
    onAppClick: ((Application) -> Unit)?,
    onUserInfoClick: ((UserInfo?) -> Unit)?,
) {

    DisposableEffect(key1 = true) {
        onDispose {
            Log.e("UserProfilePage", "onDispose")
            tabVisibilityState.value = true
        }
    }
    SideEffect {
        tabVisibilityState.value = false
    }
    var sizeOfBox by remember {
        mutableStateOf(IntSize.Zero)
    }
    var sizeOfHeaderColum by remember {
        mutableStateOf(IntSize.Zero)
    }
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                sizeOfBox = it
            }) {
        var currentShowContent by remember {
            mutableStateOf("None")
        }
        val offsetYOfContentSurface by remember {
            derivedStateOf {
                when (currentShowContent) {
                    "Application", "Screen", "Follower/Followed" -> {
                        IntOffset(0, 0)
                    }

                    "None" -> {
                        IntOffset(0, sizeOfHeaderColum.height)
                    }

                    else -> {
                        IntOffset(0, sizeOfBox.height)
                    }
                }
            }
        }
        Column(
            modifier = Modifier.onSizeChanged {
                sizeOfHeaderColum = it
            }
        ) {
            BackActionTopBar(onBackAction = onBackClick)
            UserInfoHeader(
                modifier = Modifier,
                userInfoState = userInfoState,
                userFollowersState = userFollowersState,
                userFollowedState = userFollowedState,
                myFollowedThisUserState = myFollowedThisUserState,
                onAddFriendClick = onAddFriendClick,
                onFollowStateClick = onFollowStateClick,
                onTalkToUserClick = onTalkToUserClick,
                onApplicationButtonClick = {
                    currentShowContent = "Application"
                },
                onScreenButtonClick = {
                    currentShowContent = "Screen"
                },
                onFollowerButtonClick = {
                    currentShowContent = "Follower/Followed"
                }
            )
        }
        val offsetYOfContentSurfaceState by animateIntOffsetAsState(
            targetValue = offsetYOfContentSurface,
            label = "offsetYOfContentSurfaceAnimate"
        )
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .offset { offsetYOfContentSurfaceState }
        ) {
            Column {
                if (currentShowContent != "None") {
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                    val titleText =
                        "${(userInfoState.value as UserInfoProfileState.UserInfoWrapper).userInfo?.name}的${currentShowContent}"
                    Text(
                        text = titleText, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                when (currentShowContent) {
                    "Screen" -> {
                        UsersScreens(
                            screensState = userScreensState,
                            onLoadMore = onLoadMoreScreens,
                            onPictureClick = onPictureClick,
                            onScreenContentClick = onScreenContentClick,
                            onScreenVideoPlayClick = onScreenVideoPlayClick
                        )
                    }

                    "Application" -> {
                        UsersApplications(
                            userApplicationsState = userApplicationsState,
                            onAppClick = onAppClick
                        )
                    }

                    "Follower/Followed" -> {
                        UsersFollowers(
                            userFollowersState = userFollowersState,
                            userFollowedState = userFollowedState,
                            onUserInfoClick = onUserInfoClick
                        )
                    }

                    "None" -> {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "选择一项展示内容", fontSize = 12.sp)
                        }

                    }
                }
            }

        }
        if (currentShowContent != "None") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_round_expand_more_24),
                    contentDescription = "expand",
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            currentShowContent = "None"
                        })
            }
        }

    }

}


@Composable
fun UserInfoHeader(
    modifier: Modifier,
    userInfoState: State<UserInfoProfileState>,
    userFollowersState: State<List<UserInfo>?>,
    userFollowedState: State<List<UserInfo>?>,
    myFollowedThisUserState: State<Boolean?>,
    onAddFriendClick: (UserInfo) -> Unit,
    onFollowStateClick: (UserInfo) -> Unit,
    onTalkToUserClick: (UserInfo) -> Unit,
    onApplicationButtonClick: () -> Unit,
    onScreenButtonClick: () -> Unit,
    onFollowerButtonClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        val userInfoProfileState = userInfoState.value
        LocalOrRemoteImage(
            modifier = Modifier
                .size(250.dp)
                .clip(RoundedCornerShape(32.dp)),
            any = (userInfoProfileState as? UserInfoProfileState.UserInfoWrapper)?.userInfo?.avatarUrl,
            defaultColor = MaterialTheme.colorScheme.secondaryContainer
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (userInfoProfileState is UserInfoProfileState.UserInfoWrapper) {
                userInfoProfileState.userInfo?.name ?: "Unknown"
            } else {
                (userInfoProfileState as UserInfoProfileState.Loading).tips
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (userInfoProfileState is UserInfoProfileState.UserInfoWrapper) {
                if (userInfoProfileState.userInfo?.introduction.isNullOrEmpty()) {
                    "没有简介"
                } else {
                    userInfoProfileState.userInfo?.introduction ?: "没有简介"
                }
            } else {
                (userInfoProfileState as UserInfoProfileState.Loading).tips
            }
        )
        if (userInfoProfileState is UserInfoProfileState.UserInfoWrapper) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                val sco = rememberScrollState()
                Row(modifier = Modifier.horizontalScroll(sco)) {
                    val textModifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                    val spacerWidthModifier = Modifier.width(12.dp)
                    Spacer(modifier = spacerWidthModifier)
                    SuggestionChip(onClick = onApplicationButtonClick, label = {
                        Text(text = "Application", modifier = textModifier, fontSize = 12.sp)
                    })
                    Spacer(modifier = spacerWidthModifier)
                    SuggestionChip(onClick = onScreenButtonClick, label = {
                        Text(text = "Screen", modifier = textModifier, fontSize = 12.sp)
                    })
                    Spacer(modifier = spacerWidthModifier)
                    SuggestionChip(onClick = onFollowerButtonClick, label = {
                        Text(
                            text = "${userFollowersState.value?.size ?: 0}/${userFollowedState.value?.size ?: 0} (Follower/Followed)",
                            modifier = textModifier,
                            fontSize = 12.sp
                        )
                    })
                    val realUserInfo = userInfoProfileState.userInfo
                    if (realUserInfo?.uid == LocalAccountManager._userInfo.value.uid) {
                        Spacer(modifier = spacerWidthModifier)
                        SuggestionChip(onClick = {}, label = {
                            Text(
                                text = "编辑资料",
                                modifier = textModifier,
                                fontSize = 12.sp
                            )
                        })
                        Spacer(modifier = spacerWidthModifier)
                    } else {
                        Spacer(modifier = spacerWidthModifier)
                        SuggestionChip(onClick = {
                            onFollowStateClick(realUserInfo!!)
                        }, label = {
                            Text(
                                text = if (myFollowedThisUserState.value == true) {
                                    "Cancel Follow"
                                } else {
                                    "Follow"
                                },
                                modifier = textModifier, fontSize = 12.sp
                            )
                        })
                        Spacer(modifier = spacerWidthModifier)
                        SuggestionChip(onClick = {
                            onTalkToUserClick(realUserInfo!!)
                        }, label = {
                            Text(text = "聊天", modifier = textModifier, fontSize = 12.sp)
                        })
                        if (!UserRelationsCase.getInstance()
                                .hasUserRelated(realUserInfo!!.uid)
                        ) {
                            Spacer(modifier = spacerWidthModifier)
                            SuggestionChip(onClick = {
                                onAddFriendClick(realUserInfo)
                            }, label = {
                                Text(
                                    text = "添加朋友",
                                    modifier = textModifier,
                                    fontSize = 12.sp
                                )
                            })
                            Spacer(modifier = spacerWidthModifier)
                        } else {
                            Spacer(modifier = spacerWidthModifier)
                        }
                    }
                }
            }
            Divider(
                modifier = Modifier.height(0.5.dp),
                color = MaterialTheme.colorScheme.outline
            )
        } else if (userInfoProfileState is UserInfoProfileState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = userInfoProfileState.tips)
                }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsersScreens(
    screensState: State<List<ScreenState>?>,
    onLoadMore: (() -> Unit)?,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?,
) {
    if (screensState.value.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "没有Screen", fontSize = 12.sp)
        }
    } else {
        val scrollableState =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                rememberLazyListState()
            } else {
                rememberLazyStaggeredGridState()
            }
        LoadMoreHandler(scrollableState = scrollableState) {
            onLoadMore?.invoke()
        }
        val interactionFlow: (Interaction, ScreenMediaFileUrl) -> Unit = remember {
            { _, _ -> }
        }

        ScreensList(
            modifier = Modifier,
            currentDestinationRoute = PageRouteNameProvider.UserProfilePage,
            screensState = screensState,
            scrollableState = scrollableState,
            onPictureClick = onPictureClick,
            picInteractionFlow = interactionFlow,
            onScreenAvatarClick = null,
            onScreenContentClick = onScreenContentClick,
            onScreenVideoPlayClick = onScreenVideoPlayClick
        )
    }
}

@Composable
fun UsersApplications(
    userApplicationsState: State<List<Application>?>,
    onAppClick: ((Application) -> Unit)?
) {
    if (userApplicationsState.value.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "没有Application", fontSize = 12.sp)
        }
    } else {
        LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 120.dp), content = {
            items(userApplicationsState.value!!) { application ->
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(98.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAppClick?.invoke(application)
                            },
                        any = application.iconUrl
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = application.name ?: "UnKnown App", fontSize = 16.sp)
                }
            }
            item {
                Spacer(modifier = Modifier.height(120.dp))
            }
        })
    }
}

@Composable
fun UsersFollowers(
    userFollowersState: State<List<UserInfo>?>,
    userFollowedState: State<List<UserInfo>?>,
    onUserInfoClick: ((UserInfo?) -> Unit)?,
) {
    LazyColumn(contentPadding = PaddingValues(horizontal = 12.dp), content = {
        item {
            Text(text = "Follower", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        if (userFollowersState.value.isNullOrEmpty()) {
            item {
                Text(
                    text = "没有Follower",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 52.dp)
                )
            }
        } else {
            items(userFollowersState.value!!) { userInfo ->
                Row(modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onUserInfoClick?.invoke(userInfo)
                    })
                {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        any = userInfo.avatarUrl
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = userInfo.name ?: "Unset Name")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = userInfo.introduction ?: "")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Text(text = "Followed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        if (userFollowedState.value.isNullOrEmpty()) {
            item {
                Text(
                    text = "没有Followed",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 52.dp)
                )
            }
        } else {
            items(userFollowedState.value!!) { userInfo ->
                Row(modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onUserInfoClick?.invoke(userInfo)
                    })
                {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        any = userInfo.avatarUrl
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = userInfo.name ?: "Unset Name")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = userInfo.introduction ?: "")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(120.dp))
        }
    })
}

