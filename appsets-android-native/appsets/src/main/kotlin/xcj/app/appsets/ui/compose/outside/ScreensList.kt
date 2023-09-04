package xcj.app.appsets.ui.compose.outside

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenState
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.PageRouteNameProvider

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreensList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screensState: State<List<ScreenState>?>,
    scrollableState: ScrollableState,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)? = null,
    picInteractionFlow: ((Interaction, ScreenMediaFileUrl) -> Unit)? = null,
    onScreenAvatarClick: ((UserScreenInfo) -> Unit)? = null,
    onScreenContentClick: ((UserScreenInfo) -> Unit)? = null,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)? = null,
) {
    val isShowX18ContentRequestDialog = remember {
        mutableStateOf(false)
    }
    var x18ContentConfirmCallback: (() -> Unit)? by remember {
        mutableStateOf(null)
    }
    X18ContentConfirmDialog(isShowX18ContentRequestDialog, x18ContentConfirmCallback)
    val configuration = LocalConfiguration.current
    val paddingValues = if (currentDestinationRoute == PageRouteNameProvider.OutSidePage) {
        PaddingValues(top = 68.dp, bottom = 68.dp, start = 12.dp, end = 12.dp)
    } else {
        PaddingValues(top = 12.dp, bottom = 68.dp, start = 12.dp, end = 12.dp)
    }
    val coroutineScope = rememberCoroutineScope()
    val screenStateList = screensState.value
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        LazyColumn(
            contentPadding = paddingValues,
            state = scrollableState as LazyListState
        ) {

            if (screenStateList != null) {
                itemsIndexed(screenStateList, { index, _ -> index }) { index, screenState ->
                    if (screenState is ScreenState.Screen) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            if (index >= 1) {
                                Divider(
                                    color = MaterialTheme.colorScheme.outline,
                                    thickness = 0.5.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            ScreenComponent(
                                screenState.userScreenInfo,
                                currentDestinationRoute,
                                160.dp,
                                onScreenAvatarClick,
                                onScreenContentClick,
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
                                picInteractionFlow,
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
                    } else if (screenState is ScreenState.NoMore) {
                        Box(
                            Modifier
                                .height(150.dp)
                                .fillMaxWidth(), contentAlignment = Alignment.Center
                        ) {
                            Text(text = "•••")
                        }
                    }

                }

                item {
                    Spacer(modifier = Modifier.height(98.dp))
                }
            }

        }
    } else {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(3),
            contentPadding = paddingValues,
            state = scrollableState as LazyStaggeredGridState,
            content = {
                if (screenStateList != null) {
                    itemsIndexed(screenStateList, { index, _ -> index }) { _, screenState ->
                        if (screenState is ScreenState.Screen) {
                            Surface(
                                modifier = Modifier.padding(6.dp),
                                shape = RoundedCornerShape(24.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .width(400.dp)
                                        .padding(12.dp)
                                ) {
                                    ScreenComponent(
                                        screenState.userScreenInfo,
                                        currentDestinationRoute,
                                        160.dp,
                                        onScreenAvatarClick,
                                        onScreenContentClick,
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
                                        picInteractionFlow,
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
                            }
                        } else if (screenState is ScreenState.NoMore) {
                            Box(
                                Modifier
                                    .height(150.dp)
                                    .fillMaxWidth(), contentAlignment = Alignment.Center
                            ) {
                                Text(text = "•••")
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(98.dp))
                    }
                }
            })
    }
}

@Composable
fun ScreenComponent(
    screenInfo: UserScreenInfo,
    currentDestinationRoute: String,
    pictureHeight: Dp,
    onScreenAvatarClick: ((UserScreenInfo) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    pictureInteractionFlow: ((Interaction, ScreenMediaFileUrl) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?
) {
    ScreenSectionOfUserPart(screenInfo, onScreenAvatarClick, currentDestinationRoute)
    if (screenInfo.isAllContentEmpty()) {
        ScreenSectionOfContentPartAllEmpty(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    onScreenContentClick?.invoke(screenInfo)
                })
    } else {
        Column(modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                onScreenContentClick?.invoke(screenInfo)
            }) {
            ScreenSectionOfContentTextPart(screenInfo, currentDestinationRoute)
            ScreenSectionOfContentPicturesPart(
                screenInfo = screenInfo,
                pictureHeight = pictureHeight,
                onPictureClick = onPictureClick,
                picInteractionFlow = pictureInteractionFlow
            )
            ScreenSectionOfContentVideosPart(screenInfo, pictureHeight, onScreenVideoPlayClick)
            ScreenSectionOfContentAssociateTopicsPart(screenInfo)
            ScreenSectionOfContentAssociatePeoplesPart(screenInfo)
        }
    }
}


@Composable
fun ScreenSectionOfContentAssociatePeoplesPart(
    screenInfo: UserScreenInfo
) {
    if (!screenInfo.associateUsers.isNullOrEmpty())
        Text(
            text = screenInfo.associateUsers ?: "",
            maxLines = 1,
            modifier = Modifier.padding(vertical = 6.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
}

@Composable
fun ScreenSectionOfContentAssociateTopicsPart(
    screenInfo: UserScreenInfo
) {
    if (!screenInfo.associateTopics.isNullOrEmpty())
        Text(
            text = screenInfo.associateTopics ?: "",
            maxLines = 1,
            modifier = Modifier.padding(vertical = 6.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
}

@Composable
fun ScreenSectionOfContentVideosPart(
    screenInfo: UserScreenInfo,
    pictureHeight: Dp,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?
) {
    val videoMediaFileUrl =
        screenInfo.videoMediaFileUrls?.getOrNull(0)
    if (videoMediaFileUrl != null) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(pictureHeight)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(12.dp)
                )
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    if (onScreenVideoPlayClick != null) {
                        onScreenVideoPlayClick(videoMediaFileUrl)
                    }
                }
            ) {
                if (videoMediaFileUrl.x18Content == 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val text = AnnotatedString(
                            "受限内容,继续查看",
                            listOf(
                                AnnotatedString.Range(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ), 4, 9
                                )
                            )
                        )
                        Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        any = videoMediaFileUrl.mediaFileCompanionUrl
                    )
                }

                Image(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    painter =
                    painterResource(id = R.drawable.ic_baseline_slow_motion_video_24),
                    contentDescription = null,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScreenSectionOfContentPicturesPart(
    screenInfo: UserScreenInfo,
    pictureHeight: Dp,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    picInteractionFlow: ((Interaction, ScreenMediaFileUrl) -> Unit)?
) {
    val pictureMediaFileUrls =
        screenInfo.pictureMediaFileUrls
    if (!pictureMediaFileUrls.isNullOrEmpty()) {

        val picRowCount = (pictureMediaFileUrls.size / 3 + pictureMediaFileUrls.size % 3)
        repeat(picRowCount) {
            val start = (it * 3).coerceAtMost(picRowCount)
            val end = (start + 3).coerceAtMost(pictureMediaFileUrls.size)
            if (start < end) {
                val mediaFileUrls =
                    pictureMediaFileUrls.subList(start, end)
                RowOfScreenPictures(
                    Modifier.padding(vertical = 2.dp),
                    pictureHeight,
                    pictureMediaFileUrls,
                    mediaFileUrls,
                    onPictureClick,
                    picInteractionFlow
                )
            }
        }
    }
}

@Composable
fun ScreenSectionOfContentTextPart(
    screenInfo: UserScreenInfo,
    currentDestinationRoute: String
) {
    val textStyle = MaterialTheme.typography.labelLarge
    if (!screenInfo.screenContent.isNullOrEmpty()) {
        val modifier = if (currentDestinationRoute != PageRouteNameProvider.ScreenDetailsPage) {
            Modifier.heightIn(min = 42.dp, max = 350.dp)
        } else {
            Modifier.heightIn(min = 42.dp)
        }
        Text(
            text = screenInfo.screenContent ?: "",
            modifier = modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            overflow = TextOverflow.Ellipsis,
            fontSize = textStyle.fontSize,
            fontStyle = textStyle.fontStyle,
            fontWeight = textStyle.fontWeight,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ScreenSectionOfContentPartAllEmpty(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        contentAlignment = Alignment.Center
    ) {
        val textStyle = MaterialTheme.typography.labelLarge
        Text(
            text = ":) 一条空的状态 ",
            fontSize = textStyle.fontSize,
            fontStyle = textStyle.fontStyle,
            fontWeight = textStyle.fontWeight,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ScreenSectionOfUserPart(
    screenInfo: UserScreenInfo,
    onScreenAvatarClick: ((UserScreenInfo) -> Unit)?,
    currentDestinationRoute: String
) {
    Box(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onScreenAvatarClick?.invoke(screenInfo)
                }
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalOrRemoteImage(
                any = screenInfo.userInfo?.avatarUrl,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = screenInfo.userInfo?.name ?: "",
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = screenInfo.postTime ?: "",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 12.sp
                )
            }
        }
        if (screenInfo.uid == LocalAccountManager._userInfo.value.uid
            && currentDestinationRoute == PageRouteNameProvider.UserProfilePage
        ) {
            Box(modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable {

                }
                .padding(vertical = 6.dp, horizontal = 10.dp)
            ) {
                val isPublicStr = if (screenInfo.isPublic == 1) {
                    "公开"
                } else {
                    "私有"
                }
                Text(
                    text = isPublicStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

        }
    }
}

/**
 * at least one picture, max size is 3
 */
@Composable
fun RowOfScreenPictures(
    modifier: Modifier,
    picHeight: Dp,
    allMediaFileUrls: List<ScreenMediaFileUrl>,
    mediaFileUrls: List<ScreenMediaFileUrl>,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    picInteractionFlow: ((Interaction, ScreenMediaFileUrl) -> Unit)? = null,
) {
    var sizeOfItem by remember {
        mutableStateOf(IntSize.Zero)
    }
    Row(
        modifier
            .height(picHeight)
            .fillMaxWidth()
            .onSizeChanged {
                sizeOfItem = it
            })
    {
        val singlePicWidth = with(LocalDensity.current) {
            (sizeOfItem.width / mediaFileUrls.size).toDp()
        }
        mediaFileUrls.forEachIndexed { index, mediaFileUrl ->
            if (mediaFileUrl.mediaType == "image/*") {
                val topEnd =
                    if (index == mediaFileUrls.size - 1) {
                        12.dp
                    } else {
                        0.dp
                    }
                val topStart = if (index == 0) {
                    12.dp
                } else {
                    0.dp
                }
                val shape = RoundedCornerShape(
                    topStart,
                    topEnd,
                    topEnd,
                    topStart
                )
                val interactionSource = remember {
                    MutableInteractionSource()
                }
                LaunchedEffect(
                    key1 = interactionSource,
                    block = {
                        interactionSource.interactions.collect { interaction ->
                            if (picInteractionFlow != null) {
                                picInteractionFlow(
                                    interaction,
                                    mediaFileUrl
                                )
                            }
                        }
                    })
                Surface(
                    onClick = {
                        onPictureClick?.invoke(mediaFileUrl, allMediaFileUrls)
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(singlePicWidth)
                        .clip(shape),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    if (mediaFileUrl.x18Content == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape),
                            contentAlignment = Alignment.Center
                        ) {
                            val text = AnnotatedString(
                                "受限内容,继续查看",
                                listOf(
                                    AnnotatedString.Range(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        ), 4, 9
                                    )
                                )
                            )
                            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    } else {
                        LocalOrRemoteImage(
                            any = mediaFileUrl.mediaFileUrl,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape)
                        )
                    }

                }
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
