package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenState
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.outside.LoadMoreHandler
import xcj.app.appsets.ui.compose.outside.ScreensList
import xcj.app.appsets.usecase.UserInfoProfileState
import xcj.app.appsets.usecase.UserRelationsCase

@OptIn(ExperimentalFoundationApi::class)
@UnstableApi
@Composable
fun UserProfilePage(
    tabVisibilityState: MutableState<Boolean>,
    onBackClick: () -> Unit,
    onAddFriendClick: (UserInfo) -> Unit,
    onFollowStateClick: (UserInfo) -> Unit,
    onTalkToUserClick: (UserInfo) -> Unit,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)? = null,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)? = null,
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
    val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
    val userInfoState = mainViewModel.userInfoUseCase.currentUserInfoState.value
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_arrow_24),
                    contentDescription = "go back",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = {
                            mainViewModel.screensUseCase?.removeUserContainerIfNeeded()
                            onBackClick()
                        })
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Divider(Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.padding(horizontal = 12.dp)) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        any = (userInfoState as? UserInfoProfileState.UserInfoWrapper)?.userInfo?.avatarUrl,
                        defaultColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (userInfoState is UserInfoProfileState.UserInfoWrapper) {
                                userInfoState.userInfo?.name ?: "Unknown"
                            } else {
                                (userInfoState as UserInfoProfileState.Loading).tips
                            },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (userInfoState is UserInfoProfileState.UserInfoWrapper) {
                                if (userInfoState.userInfo?.introduction.isNullOrEmpty()) {
                                    "暂未添加自我介绍"
                                } else {
                                    userInfoState.userInfo?.introduction ?: "暂未添加自我介绍"
                                }
                            } else {
                                "暂未添加自我介绍"
                            }
                        )
                    }
                }
            }

        }
        if (userInfoState is UserInfoProfileState.UserInfoWrapper) {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                val sco = rememberScrollState()
                Row(modifier = Modifier.horizontalScroll(sco)) {
                    val textModifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                    val spacerWidthModifier = Modifier.width(12.dp)
                    Spacer(modifier = spacerWidthModifier)
                    Card() {
                        Text(text = "应用", modifier = textModifier)
                    }
                    Spacer(modifier = spacerWidthModifier)
                    Card() {
                        Text(text = "8人已经关注", modifier = textModifier)
                    }
                    val realUserInfo = userInfoState.userInfo
                    if (realUserInfo?.uid == LocalAccountManager._userInfo.value.uid) {
                        Spacer(modifier = spacerWidthModifier)
                        Card() {
                            Text(text = "编辑资料", modifier = textModifier)
                        }
                        Spacer(modifier = spacerWidthModifier)
                    } else {
                        Spacer(modifier = spacerWidthModifier)
                        Card(modifier = Modifier.clickable {
                            onFollowStateClick(realUserInfo!!)
                        }) {
                            Text(text = "关注", modifier = textModifier)
                        }
                        Spacer(modifier = spacerWidthModifier)
                        Card(modifier = Modifier.clickable {
                            onTalkToUserClick(realUserInfo!!)
                        }) {
                            Text(text = "聊天", modifier = textModifier)
                        }
                        if (!UserRelationsCase.getInstance().hasUserRelated(realUserInfo!!.uid)) {
                            Spacer(modifier = spacerWidthModifier)
                            Card(modifier = Modifier.clickable {
                                onAddFriendClick(realUserInfo)
                            }) {
                                Text(text = "添加朋友", modifier = textModifier)
                            }
                            Spacer(modifier = spacerWidthModifier)
                        } else {
                            Spacer(modifier = spacerWidthModifier)
                        }
                    }
                }
            }
            Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
            val screensState = mainViewModel.screensUseCase?.userScreensContainer?.screensState
            if (screensState?.value.isNullOrEmpty() || (screensState!!.value!!.size == 1 && screensState.value?.get(
                    0
                ) is ScreenState.NoMore)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 120.dp), contentAlignment = Alignment.Center
                ) {
                    Text(text = "没有推文")
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    val scrollableState =
                        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            rememberLazyListState()
                        } else {
                            rememberLazyStaggeredGridState()
                        }
                    LoadMoreHandler(scrollableState = scrollableState) {
                        mainViewModel.screensUseCase?.loadMore()
                    }
                    val interactionFlow: (Interaction, ScreenMediaFileUrl) -> Unit = remember {
                        { _, _ -> }
                    }
                    ScreensList(
                        modifier = Modifier,
                        currentDestinationRoute = PageRouteNameProvider.UserProfilePage,
                        screensState = screensState!!,
                        scrollableState = scrollableState,
                        onPictureClick = onPictureClick,
                        picInteractionFlow = interactionFlow,
                        onScreenAvatarClick = null,
                        onScreenContentClick = onScreenContentClick,
                        onScreenVideoPlayClick = onScreenVideoPlayClick
                    )

                }
            }
        } else if (userInfoState is UserInfoProfileState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(158.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = userInfoState.tips)
                }
            }
        }

    }
}
