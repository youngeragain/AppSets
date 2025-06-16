@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.View
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.msusman.compose.cardstack.Direction
import com.msusman.compose.cardstack.Duration
import com.msusman.compose.cardstack.SwipeDirection
import com.msusman.compose.cardstack.SwipeMethod
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.rememberZoomablePeekOverlayState
import me.saket.telephoto.zoomable.zoomablePeekOverlay
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.CardStack0
import xcj.app.appsets.ui.compose.custom_component.rememberStackState0
import xcj.app.appsets.ui.model.PictureStyleState
import xcj.app.appsets.ui.model.ScreenState
import xcj.app.appsets.util.saveComposeNodeAsBitmap
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ScreensList"

@Composable
fun ScreensList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screens: List<ScreenState>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: (Interaction, ScreenMediaFileUrl) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        PortraitScreenList(
            modifier = modifier,
            scrollableState = scrollableState,
            currentDestinationRoute = currentDestinationRoute,
            screens = screens,
            onBioClick = onBioClick,
            pictureInteractionFlowCollector = pictureInteractionFlowCollector,
            onScreenMediaClick = onScreenMediaClick,
            headerContent = headerContent
        )
    } else {
        LandscapeScreenList(
            modifier = modifier,
            scrollableState = scrollableState,
            currentDestinationRoute = currentDestinationRoute,
            screens = screens,
            onBioClick = onBioClick,
            pictureInteractionFlowCollector = pictureInteractionFlowCollector,
            onScreenMediaClick = onScreenMediaClick,
            headerContent = headerContent
        )
    }
}

@Composable
fun LandscapeScreenList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screens: List<ScreenState>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: (Interaction, ScreenMediaFileUrl) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalItemSpacing = 6.dp,
        state = scrollableState as LazyStaggeredGridState
    )
    {
        if (currentDestinationRoute == PageRouteNames.OutSidePage) {
            items(3) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
        }
        item {
            headerContent?.invoke()
        }
        itemsIndexed(screens, { index, _ -> index }) { _, screenState ->
            if (screenState is ScreenState.Screen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.extraLarge
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            onBioClick(screenState.screenInfo)
                        }
                        .padding(12.dp)
                ) {
                    ScreenComponent(
                        currentDestinationRoute = currentDestinationRoute,
                        screenInfo = screenState.screenInfo,
                        onBioClick = onBioClick,
                        pictureInteractionFlowCollector = pictureInteractionFlowCollector,
                        onScreenMediaClick = onScreenMediaClick
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}

@Composable
fun PortraitScreenList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screens: List<ScreenState>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: (Interaction, ScreenMediaFileUrl) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        state = scrollableState as LazyListState
    ) {
        if (currentDestinationRoute != PageRouteNames.UserProfilePage) {
            item {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
        }
        if (headerContent != null) {
            item {
                headerContent()
            }
        }
        itemsIndexed(
            items = screens,
            key = { index, screenState ->
                if (screenState is ScreenState.Screen) {
                    screenState.screenInfo.id
                } else {
                    index
                }
            }
        ) { _, screenState ->
            if (screenState is ScreenState.Screen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.extraLarge
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable {
                            onBioClick(screenState.screenInfo)
                        }
                        .padding(12.dp)
                ) {
                    ScreenComponent(
                        currentDestinationRoute = currentDestinationRoute,
                        screenInfo = screenState.screenInfo,
                        onBioClick = onBioClick,
                        pictureInteractionFlowCollector = pictureInteractionFlowCollector,
                        onScreenMediaClick = onScreenMediaClick
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}

@Composable
fun ScreenComponent(
    currentDestinationRoute: String,
    screenInfo: ScreenInfo,
    pictureStyleState: PictureStyleState = rememberPictureStyleState(),
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit),
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    var capturingViewBounds by remember { mutableStateOf<Rect?>(null) }
    Column(
        modifier = Modifier
            .animateContentSize()
            .onGloballyPositioned {
                capturingViewBounds = it.boundsInRoot()
            }
    ) {
        if (screenInfo.isAllContentEmpty()) {
            ScreenSectionOfContentPartAllEmpty(
                modifier = Modifier.clip(MaterialTheme.shapes.large)
            )
        } else {
            Column(
                modifier = Modifier.clip(MaterialTheme.shapes.large)
            ) {
                ScreenTopActionsPart(
                    currentDestinationRoute = currentDestinationRoute,
                    screenInfo = screenInfo,
                    pictureStyleState = pictureStyleState,
                    capturingViewBounds = capturingViewBounds
                )

                ScreenSectionOfContentMediasPart(
                    screenInfo = screenInfo,
                    onMediaClick = onScreenMediaClick,
                    mediaInteractionFlowCollector = pictureInteractionFlowCollector
                )
                /* ScreenSectionOfContentPicturesPart(
                     screenInfo = screenInfo,
                     pictureStyleState = pictureStyleState,
                     onMediaClick = onScreenMediaClick,
                     picInteractionFlow = pictureInteractionFlowCollector
                 )
                 ScreenSectionOfContentVideosPart(
                     screenInfo = screenInfo,
                     pictureStyleState = pictureStyleState,
                     onMediaClick = onScreenMediaClick
                 )*/
                ScreenSectionOfContentTextPart(
                    screenInfo = screenInfo,
                    currentDestinationRoute = currentDestinationRoute
                )
                ScreenSectionOfContentAssociateTopicsPart(screenInfo)
                ScreenSectionOfContentAssociatePeoplesPart(screenInfo)
            }
        }
        ScreenSectionOfUserPart(
            screenInfo,
            onBioClick,
            currentDestinationRoute
        )
    }
}

@Composable
fun ScreenTopActionsPart(
    currentDestinationRoute: String,
    screenInfo: ScreenInfo,
    pictureStyleState: PictureStyleState,
    capturingViewBounds: Rect?,
) {
    val context = LocalContext.current
    val localView = LocalView.current
    val scope = rememberCoroutineScope()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SuggestionChip(
            onClick = {
                scope.launch {
                    shareAppSetsUserScreen(context, capturingViewBounds, localView)
                }
            },
            label = {
                Image(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_ios_share_24),
                    contentDescription = "share"
                )
            },
            shape = CircleShape
        )
        val mediaFileUrls = screenInfo.mediaFileUrls
        if (!mediaFileUrls.isNullOrEmpty()) {
            SuggestionChip(
                onClick = {

                },
                label = {
                    val videoCount = mediaFileUrls.sumOf {
                        if (it.isVideoMedia) {
                            1.toInt()
                        } else {
                            0.toInt()
                        }
                    }
                    val pictureCount = mediaFileUrls.sumOf {
                        if (!it.isVideoMedia) {
                            1.toInt()
                        } else {
                            0.toInt()
                        }
                    }
                    if (videoCount > 0 && pictureCount > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    xcj.app.appsets.R.string.x_videos,
                                    videoCount
                                ), fontSize = 12.sp
                            )
                            Text(
                                text = stringResource(
                                    xcj.app.appsets.R.string.x_pictures,
                                    pictureCount
                                ), fontSize = 12.sp
                            )
                        }
                    } else if (videoCount > 0) {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.x_videos, videoCount),
                            fontSize = 12.sp
                        )
                    } else if (pictureCount > 0) {
                        Text(
                            text = stringResource(
                                xcj.app.appsets.R.string.x_pictures,
                                pictureCount
                            ), fontSize = 12.sp
                        )
                    }
                },
                shape = CircleShape
            )
        }
    }
}

@Composable
fun ScreenSectionOfContentAssociatePeoplesPart(
    screenInfo: ScreenInfo,
) {
    if (!screenInfo.associateUsers.isNullOrEmpty())
        Text(
            text = screenInfo.associateUsers,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 6.dp),
            fontSize = 12.sp
        )
}

@Composable
fun ScreenSectionOfContentAssociateTopicsPart(
    screenInfo: ScreenInfo,
) {
    if (!screenInfo.associateTopics.isNullOrEmpty())
        Text(
            text = screenInfo.associateTopics,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 6.dp),
            fontSize = 12.sp
        )
}

@Composable
fun ScreenSectionOfContentMediasPart(
    screenInfo: ScreenInfo,
    onMediaClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    mediaInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit)?,
) {
    val mediaFileUrls = screenInfo.mediaFileUrls
    if (mediaFileUrls.isNullOrEmpty()) {
        return
    }
    val stackState0 = rememberStackState0<ScreenMediaFileUrl>(
        canDrag = false
    )
    CardStack0(
        modifier = Modifier,
        stackState0 = stackState0,
        items = mediaFileUrls,
        cardElevation = 20.dp,
        scaleRatio = 0.9f,
        rotationMaxDegree = 0,
        displacementThreshold = 60.dp,
        animationDuration = Duration.NORMAL,
        visibleCount = min(3, max(1, mediaFileUrls.size)),
        stackDirection = Direction.Top,
        swipeDirection = SwipeDirection.HORIZONTAL,
        swipeMethod = SwipeMethod.AUTOMATIC_AND_MANUAL,
        shadowElevation = 2.dp,
        shadowShape = MaterialTheme.shapes.extraLarge,
        onSwiped = { index ->
            //Log.d(TAG, "onSwiped index:$index ")

        }
    ) { mediaFileUrl ->
        MediaCard(
            mediaFileUrl,
            onMediaClick = {
                onMediaClick?.invoke(it, mediaFileUrls)
            },
            mediaInteractionFlowCollector
        )
    }
}

@Composable
fun MediaCard(
    mediaFileUrl: ScreenMediaFileUrl,
    onMediaClick: ((ScreenMediaFileUrl) -> Unit)?,
    mediaInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit)?,
) {
    Column {
        val pictureHeightOverride = 230.dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pictureHeightOverride)
                .background(
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extraLarge
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    if (onMediaClick != null) {
                        onMediaClick(mediaFileUrl)
                    }
                }
        ) {
            if (mediaFileUrl.isRestrictedContent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    val text = AnnotatedString(
                        stringResource(xcj.app.appsets.R.string.restricted_content_continue_viewing),
                        listOf(
                            AnnotatedString.Range(
                                SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                ), 5, 9
                            )
                        )
                    )
                    Text(text = text, fontSize = 12.sp)
                }
            } else {
                AnyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .zoomablePeekOverlay(rememberZoomablePeekOverlayState()),
                    any = if (mediaFileUrl.isVideoMedia) {
                        mediaFileUrl.mediaFileCompanionUrl
                    } else {
                        mediaFileUrl.mediaFileUrl
                    }
                )
                if (mediaFileUrl.isVideoMedia) {
                    IconButton(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center),
                        onClick = {
                            if (onMediaClick != null) {
                                onMediaClick(mediaFileUrl)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenSectionOfContentVideosPart(
    screenInfo: ScreenInfo,
    pictureStyleState: PictureStyleState,
    onMediaClick: ((ScreenMediaFileUrl) -> Unit)?,
) {
    val videoMediaFileUrl =
        screenInfo.mediaFileUrls?.firstOrNull { it.isVideoMedia }

    if (videoMediaFileUrl != null) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            val pictureHeightOverride = pictureStyleState.lineHeight.times(1.6f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pictureHeightOverride)
                    .background(
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        if (onMediaClick != null) {
                            onMediaClick(videoMediaFileUrl)
                        }
                    }
            ) {
                if (videoMediaFileUrl.isRestrictedContent) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        val text = AnnotatedString(
                            stringResource(xcj.app.appsets.R.string.restricted_content_continue_viewing),
                            listOf(
                                AnnotatedString.Range(
                                    SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ), 5, 9
                                )
                            )
                        )
                        Text(text = text, fontSize = 12.sp)
                    }
                } else {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge)
                            .zoomablePeekOverlay(rememberZoomablePeekOverlayState()),
                        any = videoMediaFileUrl.mediaFileCompanionUrl
                    )
                    IconButton(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.Center),
                        onClick = {
                            if (onMediaClick != null) {
                                onMediaClick(videoMediaFileUrl)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                            contentDescription = null,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ScreenSectionOfContentPicturesPart(
    screenInfo: ScreenInfo,
    pictureStyleState: PictureStyleState,
    onMediaClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    picInteractionFlow: ((Interaction, ScreenMediaFileUrl) -> Unit)?,
) {
    val pictureMediaFileUrls =
        screenInfo.mediaFileUrls?.filter { !it.isVideoMedia }
    if (pictureMediaFileUrls.isNullOrEmpty()) {
        return
    }
    val remainder = pictureMediaFileUrls.size % pictureStyleState.oneLineCount
    val temp = if (remainder > 0) {
        1
    } else {
        0
    }
    val picRowCount =
        (pictureMediaFileUrls.size / pictureStyleState.oneLineCount + temp)
    if (picRowCount > 0) {
        val pictureHeightOverride = when (pictureMediaFileUrls.size) {
            1 -> {
                pictureStyleState.lineHeight.times(1.8f)
            }

            2 -> {
                pictureStyleState.lineHeight.times(1.4f)
            }

            3 -> {
                pictureStyleState.lineHeight
            }

            4 -> {
                pictureStyleState.lineHeight.times(0.95f)
            }

            else -> {
                pictureStyleState.lineHeight.times(0.9f)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(picRowCount) { rowIndex ->
                val start =
                    (rowIndex * pictureStyleState.oneLineCount).coerceAtMost(
                        pictureMediaFileUrls.size
                    )
                val end =
                    (start + pictureStyleState.oneLineCount).coerceAtMost(
                        pictureMediaFileUrls.size
                    )
                if (start < end) {
                    val mediaFileUrls =
                        pictureMediaFileUrls.subList(start, end)
                    RowOfScreenPictures(
                        modifier = Modifier,
                        pictureMediaFileUrls,
                        mediaFileUrls,
                        picRowCount,
                        rowIndex,
                        pictureHeightOverride,
                        onMediaClick,
                        picInteractionFlow
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenSectionOfContentTextPart(
    screenInfo: ScreenInfo,
    currentDestinationRoute: String,
) {
    if (!screenInfo.screenContent.isNullOrEmpty()) {
        val modifier = if (currentDestinationRoute != PageRouteNames.ScreenDetailsPage) {
            Modifier.heightIn(min = 68.dp, max = 350.dp)
        } else {
            Modifier.heightIn(min = 68.dp)
        }
        val textStyle = MaterialTheme.typography.labelLarge
        Text(
            text = screenInfo.screenContent,
            modifier = modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            overflow = TextOverflow.Ellipsis,
            fontSize = textStyle.fontSize,
            fontStyle = textStyle.fontStyle,
            fontWeight = textStyle.fontWeight
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
            text = stringResource(xcj.app.appsets.R.string.an_empty_status),
            fontSize = textStyle.fontSize,
            fontStyle = textStyle.fontStyle,
            fontWeight = textStyle.fontWeight
        )
    }
}

@Composable
fun ScreenSectionOfUserPart(
    screenInfo: ScreenInfo,
    onBioClick: (Bio) -> Unit,
    currentDestinationRoute: String,
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = screenInfo.postTime ?: "",
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        if (screenInfo.uid == LocalAccountManager.userInfo.uid
            && currentDestinationRoute == PageRouteNames.UserProfilePage
        ) {
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clickable {

                    }
                    .padding(vertical = 6.dp, horizontal = 10.dp)
            ) {
                val isPublicStr = if (screenInfo.isPublic == 1) {
                    stringResource(xcj.app.appsets.R.string.public_)
                } else {
                    stringResource(xcj.app.appsets.R.string.private_)
                }
                Text(
                    text = isPublicStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

        } else {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        screenInfo.userInfo?.let { onBioClick.invoke(it) }
                    }
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            )
            {
                Text(
                    text = screenInfo.userInfo?.name ?: "",
                    fontSize = 12.sp
                )
                AnyImage(
                    any = screenInfo.userInfo?.bioUrl,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(10.dp))
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
    allMediaFileUrls: List<ScreenMediaFileUrl>,
    mediaFileUrls: List<ScreenMediaFileUrl>,
    rowCount: Int,
    rowIndex: Int,
    picHeight: Dp,
    onMediaClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    mediaInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit)? = null,
) {
    var sizeOfItem by remember {
        mutableStateOf(IntSize.Zero)
    }
    Row(
        modifier
            .height(picHeight)
            .fillMaxWidth()
            .animateContentSize()
            .onSizeChanged {
                sizeOfItem = it
            },
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        val singlePicWidth = with(LocalDensity.current) {
            (sizeOfItem.width / mediaFileUrls.size).toDp()
        }
        val bigCornerSize = 24.dp
        val smallCornerSize = 0.dp
        mediaFileUrls.forEachIndexed { index, mediaFileUrl ->
            if (mediaFileUrl.mediaType == ContentType.IMAGE) {

                val interactionSource = remember {
                    if (mediaInteractionFlowCollector != null) {
                        MutableInteractionSource()
                    } else {
                        null
                    }
                }
                if (mediaInteractionFlowCollector != null) {
                    LaunchedEffect(
                        key1 = interactionSource,
                        block = {
                            interactionSource?.interactions?.collect { interaction ->
                                mediaInteractionFlowCollector(
                                    interaction,
                                    mediaFileUrl
                                )
                            }
                        }
                    )
                }

                val shape = getScreenPicShape(
                    mediaFileUrls.size,
                    index,
                    rowCount,
                    rowIndex,
                    bigCornerSize,
                    smallCornerSize
                )
                Surface(
                    onClick = {
                        onMediaClick?.invoke(mediaFileUrl, allMediaFileUrls)
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(singlePicWidth)
                        .clip(shape),
                    color = MaterialTheme.colorScheme.outline
                ) {
                    if (mediaFileUrl.isRestrictedContent) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape),
                            contentAlignment = Alignment.Center
                        ) {
                            val text = AnnotatedString(
                                stringResource(id = xcj.app.appsets.R.string.restricted_content_continue_viewing),
                                listOf(
                                    AnnotatedString.Range(
                                        SpanStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        ), 5, 9
                                    )
                                )
                            )
                            Text(text = text, fontSize = 12.sp)
                        }
                    } else {
                        AnyImage(
                            any = mediaFileUrl.mediaFileUrl,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(shape)
                                .zoomablePeekOverlay(rememberZoomablePeekOverlayState()),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberPictureStyleState(): PictureStyleState {
    val pictureStyleState = remember {
        PictureStyleState()
    }
    return pictureStyleState
}

private fun shareAppSetsUserScreen(
    context: Context,
    capturingViewBounds: Rect?,
    localView: View,
) {
    val bounds = capturingViewBounds ?: run {
        PurpleLogger.current.d(TAG, "share failure, because bounds is null!")
        return
    }
    val file = saveComposeNodeAsBitmap(
        context,
        bounds,
        localView
    )
    runCatching {
        val fileUri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            file
        )
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Example: content://com.google.android.apps.photos.contentprovider/...
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "image/jpeg"
        }
        context.startActivity(
            Intent.createChooser(
                shareIntent,
                "Share my AppSets Screen"
            )
        )
    }.onFailure {
        PurpleLogger.current.e(TAG, "share failure, because invoke share sheet failure!")
    }
}

private fun getScreenPicShape(
    count: Int,
    index: Int,
    rowCount: Int,
    rowIndex: Int,
    bigCornerSize: Dp,
    smallCornerSize: Dp,
): Shape {
    val topStart = if (index == 0 && rowIndex == 0) {
        bigCornerSize
    } else {
        smallCornerSize
    }
    val topEnd =
        if (index == count - 1 && rowIndex == 0) {
            bigCornerSize
        } else {
            smallCornerSize
        }
    val bottomStart = if (index == 0 && (rowIndex == rowCount - 1)) {
        bigCornerSize
    } else {
        smallCornerSize
    }
    val bottomEnd =
        if (index == count - 1 && (rowIndex == rowCount - 1)) {
            bigCornerSize
        } else {
            smallCornerSize
        }
    val shape = RoundedCornerShape(
        topStart,
        topEnd,
        bottomEnd,
        bottomStart
    )
    return shape
}