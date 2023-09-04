package xcj.app.appsets.ui.compose.outside

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.NoneLineTextField
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.compose_share.compose.clickableSingle

@Composable
fun ScreenDetailsPage(
    tabVisibilityState: MutableState<Boolean>,
    screenInfoState: State<UserScreenInfo?>,
    screenViewCountState: State<Int?>,
    screenLikeCountState: State<Int?>,
    screenIsCollectByUserState: State<Boolean?>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onCollectClick: (String?) -> Unit,
    onLikesClick: () -> Unit,
    onUserAvatarClick: (UserInfo?) -> Unit,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenVideoPlayClick: (ScreenMediaFileUrl) -> Unit
) {
    val mainViewModel = viewModel<MainViewModel>(LocalContext.current as AppCompatActivity)
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Box(Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            var isShowLikeBigIconAnimation by remember {
                mutableStateOf(false)
            }
            var isShowCollectedIconAnimation by remember {
                mutableStateOf(false)
            }
            var isShowCollectEditBox by remember {
                mutableStateOf(false)
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_arrow_24),
                        contentDescription = "go back",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = {
                                onBackClick()
                            })
                            .padding(12.dp)
                    )
                    Text(
                        text = "Screen 展开",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${screenViewCountState.value ?: 0}", fontSize = 12.sp)
                            Icon(
                                painterResource(id = R.drawable.outline_remove_red_eye_24),
                                "browser counts",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .padding(12.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${screenLikeCountState.value ?: 0}", fontSize = 12.sp)
                            var likeItClickCounter by remember {
                                mutableStateOf(0)
                            }
                            if (likeItClickCounter > 0) {
                                LaunchedEffect(key1 = likeItClickCounter, block = {
                                    isShowLikeBigIconAnimation = true
                                    delay(350)
                                    isShowLikeBigIconAnimation = false
                                })
                            }
                            Icon(
                                painterResource(id = R.drawable.outline_favorite_border_24),
                                "like it",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickableSingle(role = Role.Button) {
                                        onLikesClick.invoke()
                                        likeItClickCounter = likeItClickCounter.inc()
                                    }
                                    .padding(12.dp)
                            )
                        }
                        if (LocalAccountManager.isMe(screenInfoState.value?.userInfo?.uid ?: "")) {
                            Icon(
                                painterResource(id = R.drawable.outline_edit_24),
                                "edit",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickableSingle(role = Role.Button) {
                                        onEditClick.invoke()
                                    }
                                    .padding(12.dp)
                            )
                        } else {
                            val resId = if (screenIsCollectByUserState.value == true) {
                                R.drawable.baseline_cruelty_free_24
                            } else {
                                R.drawable.outline_cruelty_free_24
                            }

                            Icon(
                                painterResource(id = resId),
                                "collect it",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickableSingle(role = Role.Button) {
                                        if (screenIsCollectByUserState.value == true) {
                                            onCollectClick.invoke(null)
                                        } else {
                                            isShowCollectEditBox = true
                                        }
                                    }
                                    .padding(12.dp)
                            )
                        }
                    }
                    if (!LocalAccountManager.isLogged()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Divider(
                            Modifier
                                .height(2.dp)
                                .width(42.dp), color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "需要登录", fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }

            }
            Spacer(modifier = Modifier.height(10.dp))
            Divider(Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                AnimatedVisibility(visible = isShowCollectEditBox) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                            modifier = Modifier.widthIn(min = 240.dp, max = 320.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                Modifier
                                    .fillMaxSize(1f)
                                    .padding(12.dp)
                            ) {
                                Row {
                                    Text(text = "添加收藏分类")
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        modifier = Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                            .clip(CircleShape)
                                            .clickable {
                                                isShowCollectEditBox = false
                                            }
                                            .padding(12.dp),
                                        painter = painterResource(id = R.drawable.ic_round_close_24),
                                        contentDescription = "close",
                                        tint = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                var collectCategory by remember {
                                    mutableStateOf("")
                                }
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = collectCategory,
                                    onValueChange = {
                                        collectCategory = it
                                    },
                                    placeholder = {
                                        Text(text = "默认")
                                    },
                                    maxLines = 1,
                                    shape = RoundedCornerShape(28.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                var collectItClickCounter by remember {
                                    mutableStateOf(0)
                                }
                                if (collectItClickCounter > 0) {
                                    LaunchedEffect(key1 = collectItClickCounter, block = {
                                        isShowCollectedIconAnimation = true
                                        delay(350)
                                        isShowCollectedIconAnimation = false
                                    })
                                }
                                Row {
                                    Spacer(modifier = Modifier.weight(1f))
                                    Button(onClick = {
                                        isShowCollectEditBox = false
                                        onCollectClick.invoke(collectCategory)
                                        collectItClickCounter = collectItClickCounter.inc()
                                    }) {
                                        Text(text = "确定")
                                    }
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(visible = isShowLikeBigIconAnimation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(150.dp),
                            painter = painterResource(id = R.drawable.baseline_favorite_24),
                            tint = Color(0xfff8312f),
                            contentDescription = "favorite"
                        )
                    }
                }
                AnimatedVisibility(visible = isShowCollectedIconAnimation) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val resId = if (screenIsCollectByUserState.value == false) {
                            R.drawable.baseline_cruelty_free_24
                        } else {
                            R.drawable.outline_cruelty_free_24
                        }
                        Icon(
                            modifier = Modifier.size(150.dp),
                            painter = painterResource(id = resId),
                            tint = Color(0xff3cbef5),
                            contentDescription = "collect it"
                        )
                    }
                }
                if (screenInfoState.value == null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp), contentAlignment = Alignment.Center
                    ) {
                        Text(text = "没有找到对应的Screen")
                    }
                } else {
                    val isShowX18ContentRequestDialog = remember {
                        mutableStateOf(false)
                    }
                    var x18ContentConfirmCallback: (() -> Unit)? by remember {
                        mutableStateOf(null)
                    }
                    X18ContentConfirmDialog(
                        isShowX18ContentRequestDialog,
                        x18ContentConfirmCallback
                    )
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        ScreenComponent(
                            screenInfo = screenInfoState.value!!,
                            currentDestinationRoute = PageRouteNameProvider.ScreenDetailsPage,
                            pictureHeight = 160.dp,
                            onScreenAvatarClick = { userScreenInfo ->
                                onUserAvatarClick.invoke(userScreenInfo.userInfo)
                            },
                            onScreenContentClick = null,
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
                            pictureInteractionFlow = null,
                            onScreenVideoPlayClick = { url ->
                                if (url.x18Content == 1) {
                                    x18ContentConfirmCallback = {
                                        onScreenVideoPlayClick.invoke(url)
                                    }
                                    isShowX18ContentRequestDialog.value = true
                                } else {
                                    onScreenVideoPlayClick.invoke(url)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(10.dp))
                        val currentViewScreenReviews =
                            mainViewModel.screensUseCase?.currentViewScreenReviews
                        ScreenReviews(currentViewScreenReviews, onUserAvatarClick)
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .navigationBarsPadding()
                .imePadding()
        ) {
            Divider(Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NoneLineTextField(
                    modifier = Modifier.weight(1f),
                    value = mainViewModel.screensUseCase?.currentViewScreenUserReviewState?.value
                        ?: "",
                    onValueChange = {
                        mainViewModel.screensUseCase?.currentViewScreenUserReviewState?.value = it
                    },
                    placeholder = {
                        Text(text = "添加回复")
                    },
                    maxLines = 5,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = {
                    mainViewModel.screensUseCase?.addScreenReview()
                }) {
                    Text(text = "确定")
                }
            }
        }

    }

}

@Composable
fun ScreenReviews(
    currentViewScreenReviews: MutableList<ScreenReview>?,
    onUserAvatarClick: (UserInfo?) -> Unit
) {
    if (!currentViewScreenReviews.isNullOrEmpty()) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = "下列回复",
                modifier = Modifier.padding(vertical = 18.dp)
            )
            for (reversedIndex in (currentViewScreenReviews.size - 1 downTo 0)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {

                    val review = currentViewScreenReviews[reversedIndex]
                    Column {
                        Text(
                            text = "#${(reversedIndex + 1)}",
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LocalOrRemoteImage(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    onUserAvatarClick.invoke(review.userInfo)
                                },
                            any = review.userInfo?.avatarUrl ?: ""
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "${review.userInfo?.name ?: ""} | ${review.reviewTime ?: "1970-01-01"}",
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = review.content ?: "", fontSize = 13.sp)
                    }


                }
            }
            Spacer(modifier = Modifier.height(240.dp))
        }
    } else {
        Box(
            Modifier
                .height(540.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(text = "没有回复")
        }
    }
}
