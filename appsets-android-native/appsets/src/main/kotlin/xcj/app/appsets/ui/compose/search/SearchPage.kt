package xcj.app.appsets.ui.compose.search

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.NoneLineTextField
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.outside.ScreenComponent
import xcj.app.appsets.ui.compose.outside.X18ContentConfirmDialog
import xcj.app.appsets.usecase.SearchState
import xcj.app.appsets.usecase.models.Application


@UnstableApi
@Composable
fun SearchPageResults(
    sizeOfSearchBar: IntSize,
    onAppClick: (Application) -> Unit,
    onUserInfoClick: ((UserInfo?) -> Unit)?,
    onGroupInfoClick: ((GroupInfo) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?
) {
    val vm: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
    val searchResultListState = vm.searchUseCase!!.searchResultListState
    val heightOfBox = animateDpAsState(
        targetValue = if (searchResultListState.isEmpty()) {
            120.dp
        } else if (searchResultListState.size == 1) {
            val searchState = searchResultListState[0]
            if ((searchState is SearchState.Searching) ||
                (searchState is SearchState.SearchingFailed)
            ) {
                340.dp
            } else {
                120.dp
            }
        } else {
            680.dp
        },
        animationSpec = tween(450),
        label = "search_container_height_state"
    )
    Box(
        Modifier
            .height(heightOfBox.value)
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(
                    topStart = 8.dp.value,
                    topEnd = 8.dp.value,
                    bottomStart = sizeOfSearchBar.height
                        .div(2)
                        .toFloat(),
                    bottomEnd = sizeOfSearchBar.height
                        .div(2)
                        .toFloat()
                )
            )
            .padding(horizontal = 2.dp)
    ) {
        if (searchResultListState.isEmpty()) {
            Text(text = "没有任何内容", modifier = Modifier.align(Alignment.Center))
        } else if (searchResultListState.size == 1) {
            val searchState = searchResultListState[0]
            if (searchState is SearchState.Searching) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = searchState.tips)
                }
            } else if (searchState is SearchState.SearchingFailed) {
                Text(text = searchState.tips, modifier = Modifier.align(Alignment.Center))
            }
        } else {
            val isShowX18ContentRequestDialog = remember {
                mutableStateOf(false)
            }
            var x18ContentConfirmCallback: (() -> Unit)? by remember {
                mutableStateOf(null)
            }
            X18ContentConfirmDialog(isShowX18ContentRequestDialog, x18ContentConfirmCallback)
            val rememberScrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(rememberScrollState)) {
                searchResultListState.forEachIndexed { index, result ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    when (result) {
                        is SearchState.SplitTitle -> {
                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                Text(
                                    text = result.title, fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(
                                    modifier = Modifier.height(0.5.dp),
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        is SearchState.SearchedUser -> {
                            SearchedUserComponent(
                                modifier = Modifier.clickable {
                                    onUserInfoClick?.invoke(result.userInfo)
                                }, result.userInfo
                            )
                        }

                        is SearchState.SearchedGroup -> {
                            SearchedGroupComponent(modifier = Modifier.clickable {
                                onGroupInfoClick?.invoke(result.groupInfo)
                            }, result.groupInfo)
                        }

                        is SearchState.SearchedScreen -> {
                            SearchedScreenComponent(
                                modifier = Modifier,
                                screenInfo = result.screenInfo,
                                onAvatarClick = { userInfo ->
                                    onUserInfoClick?.invoke(userInfo)
                                },
                                onScreenContentClick = onScreenContentClick,
                                onPictureClick = { url, urls ->
                                    if (url.x18Content == 1) {
                                        x18ContentConfirmCallback = {
                                            onPictureClick?.invoke(url, urls)
                                        }
                                        isShowX18ContentRequestDialog.value = true
                                    } else {
                                        onPictureClick?.invoke(url, urls)
                                    }
                                },
                                onScreenVideoPlayClick = { url ->
                                    if (url.x18Content == 1) {
                                        x18ContentConfirmCallback = {
                                            onScreenVideoPlayClick?.invoke(url)
                                        }
                                        isShowX18ContentRequestDialog.value = true
                                    } else {
                                        onScreenVideoPlayClick?.invoke(url)
                                    }
                                }
                            )
                        }

                        is SearchState.SearchedApplication -> {
                            SearchedApplicationComponent(
                                modifier = Modifier,
                                application = result.application,
                                onAppClick = onAppClick
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}


@Composable
fun SearchedUserComponent(modifier: Modifier, userInfo: UserInfo) {
    Row(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        LocalOrRemoteImage(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp)),
            any = userInfo.avatarUrl
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column() {
            Text(text = userInfo.name ?: "Unset Name")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = userInfo.introduction ?: "")
        }
    }
}

@Composable
fun SearchedGroupComponent(modifier: Modifier, groupInfo: GroupInfo) {
    Row(modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        LocalOrRemoteImage(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp)),
            any = groupInfo.iconUrl,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column() {
            Text(text = groupInfo.name ?: "Unset GroupName")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = groupInfo.introduction ?: "")
        }
    }
}

@Composable
fun SearchedScreenComponent(
    modifier: Modifier,
    screenInfo: UserScreenInfo,
    onAvatarClick: ((UserInfo?) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?
) {
    Column(
        modifier = modifier
            .padding(12.dp)
            .background(Color.Transparent)
    ) {
        ScreenComponent(
            screenInfo = screenInfo,
            currentDestinationRoute = PageRouteNameProvider.SearchPage,
            pictureHeight = 160.dp,
            onScreenAvatarClick = { userScreenInfo ->
                onAvatarClick?.invoke(userScreenInfo.userInfo)
            },
            onScreenContentClick = onScreenContentClick,
            onPictureClick = onPictureClick,
            pictureInteractionFlow = null,
            onScreenVideoPlayClick = onScreenVideoPlayClick
        )
    }
}

@Composable
fun SearchedApplicationComponent(
    modifier: Modifier,
    application: Application,
    onAppClick: (Application) -> Unit
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                    onAppClick(application)
                },
            any = application.iconUrl
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = application.name ?: "UnKnown App", fontSize = 16.sp)
    }
}


@UnstableApi
@Composable
fun SearchPage(
    tabVisibilityState: MutableState<Boolean>,
    searchStringState: String?,
    onBackClick: () -> Unit,
    onKeywordsInput: (String) -> Unit,
    onAppClick: (Application) -> Unit,
    onUserInfoClick: ((UserInfo?) -> Unit)?,
    onGroupInfoClick: ((GroupInfo) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?
) {
    val vm: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
    DisposableEffect(key1 = true, effect = {
        vm.searchUseCase?.attachToSearchFlow()
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        
        var sizeOfSearchBar by remember {
            mutableStateOf(IntSize.Zero)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            var inputContent by remember {
                mutableStateOf( "")
            }
            LaunchedEffect(key1 = true, block = {
                if(!searchStringState.isNullOrEmpty()){
                    inputContent = searchStringState
                }
            })
            NoneLineTextField(
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onBackClick)
                            .padding(4.dp),
                        painter = painterResource(id = R.drawable.ic_round_arrow_24),
                        contentDescription = "go back"
                    )
                },
                trailingIcon = {
                    Icon(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                onKeywordsInput(inputContent)
                            }
                            .padding(4.dp),
                        imageVector = Icons.Default.Search,
                        contentDescription = "search"
                    )
                },
                placeholder = {
                    Text(text = "在此键入以搜索")
                },
                value = inputContent,
                maxLines = 1,
                onValueChange = {
                    inputContent = it
                    onKeywordsInput(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .onPlaced {
                        sizeOfSearchBar = it.size
                    },
                shape = RoundedCornerShape(
                    topStart = sizeOfSearchBar.height.div(2).toFloat(),
                    topEnd = sizeOfSearchBar.height.div(2).toFloat(),
                    bottomStart = 8.dp.value,
                    bottomEnd = 8.dp.value
                )
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        SearchPageResults(
            sizeOfSearchBar = sizeOfSearchBar,
            onAppClick = onAppClick,
            onUserInfoClick = onUserInfoClick,
            onGroupInfoClick = onGroupInfoClick,
            onScreenContentClick = onScreenContentClick,
            onPictureClick = onPictureClick,
            onScreenVideoPlayClick = onScreenVideoPlayClick
        )
    }
}

