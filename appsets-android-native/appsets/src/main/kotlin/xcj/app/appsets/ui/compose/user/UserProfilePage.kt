package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
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
import xcj.app.compose_share.compose.BackActionTopBar

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

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        BackActionTopBar(onBackAction = onBackClick)
        val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
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
        val screensState = mainViewModel.screensUseCase?.userScreensContainer?.screensState
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
        ) {
            val userInfoState = mainViewModel.userInfoUseCase.currentUserInfoState.value
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LocalOrRemoteImage(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    any = (userInfoState as? UserInfoProfileState.UserInfoWrapper)?.userInfo?.avatarUrl,
                    defaultColor = MaterialTheme.colorScheme.secondaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
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
                if (userInfoState is UserInfoProfileState.UserInfoWrapper) {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        val sco = rememberScrollState()
                        Row(modifier = Modifier.horizontalScroll(sco)) {
                            val textModifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            val spacerWidthModifier = Modifier.width(12.dp)
                            Spacer(modifier = spacerWidthModifier)
                            SuggestionChip(onClick = {}, label = {
                                Text(text = "应用", modifier = textModifier, fontSize = 12.sp)
                            })
                            Spacer(modifier = spacerWidthModifier)
                            SuggestionChip(onClick = {}, label = {
                                Text(
                                    text = "8人已经关注",
                                    modifier = textModifier,
                                    fontSize = 12.sp
                                )
                            })
                            val realUserInfo = userInfoState.userInfo
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
                                    Text(text = "关注", modifier = textModifier, fontSize = 12.sp)
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

                    if (screensState.value.isNullOrEmpty() || (screensState.value!!.size == 1 && screensState.value?.get(
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

    }
}
