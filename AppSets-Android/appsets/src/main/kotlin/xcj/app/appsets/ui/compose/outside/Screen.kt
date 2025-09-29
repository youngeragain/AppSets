@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.rememberZoomablePeekOverlayState
import me.saket.telephoto.zoomable.zoomablePeekOverlay
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.util.saveComposeNodeAsBitmap
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "Screen"

@Composable
fun Screen(
    currentDestinationRoute: String,
    screenInfo: ScreenInfo,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit),
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    var capturingViewBounds by remember { mutableStateOf<Rect?>(null) }
    Column(
        modifier = Modifier
            .onGloballyPositioned {
                capturingViewBounds = it.boundsInRoot()
            }
    ) {
        if (screenInfo.isAllContentEmpty()) {
            ScreenSectionOfContentPartAllEmpty()
        } else {
            Column {
                ScreenSectionOfTopActionsPart(
                    currentDestinationRoute = currentDestinationRoute,
                    screenInfo = screenInfo,
                    capturingViewBounds = capturingViewBounds
                )

                ScreenSectionOfContentMediasPart(
                    screenInfo = screenInfo,
                    onMediaClick = onScreenMediaClick,
                    mediaInteractionFlowCollector = pictureInteractionFlowCollector
                )
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
private fun ScreenSectionOfContentPartAllEmpty(modifier: Modifier = Modifier) {
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
private fun ScreenSectionOfTopActionsPart(
    currentDestinationRoute: String,
    screenInfo: ScreenInfo,
    capturingViewBounds: Rect?,
) {
    val context = LocalContext.current
    val localView = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SuggestionChip(
            onClick = {
                coroutineScope.launch {
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
                            1
                        } else {
                            0
                        }
                    }
                    val pictureCount = mediaFileUrls.sumOf {
                        if (!it.isVideoMedia) {
                            1
                        } else {
                            0
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
private fun ScreenSectionOfContentAssociatePeoplesPart(
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
private fun ScreenSectionOfContentAssociateTopicsPart(
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
private fun ScreenSectionOfContentMediasPart(
    screenInfo: ScreenInfo,
    onMediaClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    mediaInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit)?,
) {
    val mediaFileUrls = screenInfo.mediaFileUrls
    if (mediaFileUrls.isNullOrEmpty()) {
        return
    }
    val carouselState = rememberCarouselState { mediaFileUrls.size }
    HorizontalUncontainedCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        itemWidth = 250.dp,
        itemSpacing = 8.dp,
    ) { index ->
        val mediaFileUrl = mediaFileUrls[index]
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
private fun MediaCard(
    mediaFileUrl: ScreenMediaFileUrl,
    onMediaClick: ((ScreenMediaFileUrl) -> Unit)?,
    mediaInteractionFlowCollector: ((Interaction, ScreenMediaFileUrl) -> Unit)?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                MaterialTheme.colorScheme.outline
            )
            .clickable {
                if (onMediaClick != null) {
                    onMediaClick(mediaFileUrl)
                }
            }
    ) {
        if (mediaFileUrl.isRestrictedContent) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
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
                    .zoomablePeekOverlay(rememberZoomablePeekOverlayState()),
                model = if (mediaFileUrl.isVideoMedia) {
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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

@Composable
private fun ScreenSectionOfContentTextPart(
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
private fun ScreenSectionOfUserPart(
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
                    text = screenInfo.userInfo?.bioName ?: "",
                    fontSize = 12.sp
                )
                AnyImage(
                    model = screenInfo.userInfo?.bioUrl,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    error = screenInfo.userInfo?.bioName
                )
            }
        }
    }
}

private suspend fun shareAppSetsUserScreen(
    context: Context,
    capturingViewBounds: Rect?,
    localView: View,
) {
    if (capturingViewBounds == null) {
        PurpleLogger.current.d(TAG, "share failure, because bounds is null!")
        return
    }
    if (context !is Activity) {
        PurpleLogger.current.d(TAG, "share failure, context is not Activity!")
        return
    }
    val bounds = capturingViewBounds
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
