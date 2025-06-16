@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.appsets.ui.compose.outside

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBottomBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.model.ViewScreenInfo
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.modifier.combinedClickableSingle
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState

@Composable
fun ScreenDetailsPage(
    viewScreenInfo: ViewScreenInfo,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onEditClick: () -> Unit,
    onCollectClick: (String?) -> Unit,
    onLikesClick: () -> Unit,
    onInputReview: (String) -> Unit,
    onReviewConfirm: () -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onPageShowPrevious: () -> Unit,
    onPageShowNext: () -> Unit
) {
    HideNavBarWhenOnLaunch()
    if (viewScreenInfo.screenInfo == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.no_corresponding_screen_found),
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
            DesignBottomBackButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }

    } else {
        var isShowLikeBigIconAnimation by remember {
            mutableStateOf(false)
        }

        val scope = rememberCoroutineScope()
        val hazeState = remember {
            HazeState()
        }
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    val backgroundImage =
                        viewScreenInfo.screenInfo.mediaFileUrls?.firstOrNull {
                            if (it.isVideoMedia) {
                                !it.mediaFileCompanionUrl.isNullOrEmpty()
                            } else {
                                it.mediaFileUrl.isNotEmpty()
                            }
                        }
                    if (backgroundImage != null) {
                        val backgroundImageUrl = if (backgroundImage.isVideoMedia) {
                            backgroundImage.mediaFileCompanionUrl
                        } else {
                            backgroundImage.mediaFileUrl
                        }
                        AnyImage(
                            modifier = Modifier
                                .matchParentSize()
                                .blur(30.dp, BlurredEdgeTreatment.Unbounded),
                            any = backgroundImageUrl
                        )
                    }

                    Column {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        val hapticFeedback = LocalHapticFeedback.current
                        ScreenDetailsTopBar(
                            viewScreenInfo = viewScreenInfo,
                            onBackClick = onBackClick,
                            onBioClick = onBioClick,
                            onEditClick = onEditClick,
                            onCollectClick = onCollectClick,
                            onLikesClick = {
                                onLikesClick()
                                scope.launch {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isShowLikeBigIconAnimation = true
                                }
                            }
                        )
                    }
                }
                ScreenDetailsPager(
                    hazeState = hazeState,
                    viewScreenInfo = viewScreenInfo,
                    onBioClick = onBioClick,
                    onScreenMediaClick = onScreenMediaClick,
                    onPageShowPrevious = onPageShowPrevious,
                    onPageShowNext = onPageShowNext
                )
            }

            AddReviewSpace(
                modifier = Modifier.align(Alignment.BottomCenter),
                hazeState = hazeState,
                viewScreenInfo = viewScreenInfo,
                onInputReview = onInputReview,
                onReviewConfirm = onReviewConfirm
            )

            val likeTargetSizeDp = animateDpAsState(
                targetValue = if (isShowLikeBigIconAnimation) {
                    250.dp
                } else {
                    0.dp
                },
                animationSpec = spring(
                    Spring.DampingRatioMediumBouncy,
                    Spring.StiffnessLow
                ),
                finishedListener = {
                    if (isShowLikeBigIconAnimation) {
                        isShowLikeBigIconAnimation = false
                    }
                },
                label = "like_size_animate"
            )
            Icon(
                modifier = Modifier
                    .size(likeTargetSizeDp.value)
                    .align(Alignment.Center),
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_favorite_24),
                tint = Color(0xfff8312f),
                contentDescription = stringResource(xcj.app.appsets.R.string.favorite)
            )
        }
    }
}

@Composable
fun AddReviewSpace(
    modifier: Modifier,
    hazeState: HazeState,
    viewScreenInfo: ViewScreenInfo,
    onInputReview: (String) -> Unit,
    onReviewConfirm: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .hazeEffect(hazeState, HazeMaterials.thin()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DesignTextField(
                modifier = Modifier
                    .weight(1f),
                value = viewScreenInfo.userInputReview ?: "",
                onValueChange = onInputReview,
                placeholder = {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.add_reply),
                        fontSize = 12.sp
                    )
                },
                textStyle = TextStyle.Default.copy(fontSize = 12.sp),
                maxLines = 1
            )
            AnimatedVisibility(visible = !viewScreenInfo.userInputReview.isNullOrEmpty()) {
                Row {
                    Spacer(modifier = Modifier.width(10.dp))
                    FilledTonalButton(
                        onClick = onReviewConfirm
                    ) {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.sure),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
fun ScreenDetailsPager(
    hazeState: HazeState,
    viewScreenInfo: ViewScreenInfo,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onPageShowPrevious: () -> Unit,
    onPageShowNext: () -> Unit
) {
    val pagerState = rememberPagerState { 1 }
    VerticalPager(
        state = pagerState
    ) { pageIndex ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp),
            ) {
                SelectionContainer {
                    ScreenComponent(
                        currentDestinationRoute = PageRouteNames.ScreenDetailsPage,
                        screenInfo = viewScreenInfo.screenInfo!!,
                        onBioClick = onBioClick,
                        pictureInteractionFlowCollector = { a, b -> },
                        onScreenMediaClick = onScreenMediaClick,
                    )
                }

            }
            DesignHDivider()
            ScreenReviews(viewScreenInfo.reviews, onBioClick)
        }
    }

}

@Composable
fun ScreenDetailsTopBar(
    viewScreenInfo: ViewScreenInfo,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onEditClick: () -> Unit,
    onCollectClick: (String?) -> Unit,
    onLikesClick: () -> Unit,
) {
    val anyStateProvider = LocalAnyStateProvider.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_arrow_back_24),
                    contentDescription = stringResource(id = xcj.app.appsets.R.string.return_),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = {
                            onBackClick()
                        })
                        .padding(12.dp)
                )
                Text(
                    text = stringResource(xcj.app.appsets.R.string.screen_expand),
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
                        AnimatedContent(
                            targetState = viewScreenInfo.viewCount,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            contentAlignment = Alignment.Center,
                            label = "view_count_animate"
                        ) { viewCount ->
                            Text(text = "$viewCount", fontSize = 12.sp)
                        }
                        Icon(
                            painterResource(id = xcj.app.compose_share.R.drawable.ic_outline_remove_red_eye_24),
                            stringResource(xcj.app.appsets.R.string.browser_counts),
                            modifier = Modifier
                                .clip(CircleShape)
                                .padding(12.dp)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AnimatedContent(
                            targetState = viewScreenInfo.likedCount,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            contentAlignment = Alignment.Center,
                            label = "like_count_animate"
                        ) { likeCount ->
                            Text(text = "$likeCount", fontSize = 12.sp)
                        }

                        Icon(
                            painterResource(id = xcj.app.compose_share.R.drawable.ic_outline_favorite_border_24),
                            stringResource(xcj.app.appsets.R.string.like_it),
                            modifier = Modifier
                                .clip(CircleShape)
                                .combinedClickableSingle(role = Role.Button) {
                                    onLikesClick()
                                }
                                .padding(12.dp)
                        )
                    }
                    if (
                        LocalAccountManager.isLoggedUser(viewScreenInfo.screenInfo?.userInfo?.uid)
                    ) {
                        Icon(
                            painterResource(id = xcj.app.compose_share.R.drawable.ic_edit_24),
                            stringResource(xcj.app.appsets.R.string.edit),
                            modifier = Modifier
                                .clip(CircleShape)
                                .combinedClickableSingle(role = Role.Button) {
                                    onEditClick()
                                }
                                .padding(12.dp)
                        )
                    } else {
                        val resId = if (viewScreenInfo.isCollectedByUser) {
                            xcj.app.compose_share.R.drawable.ic_round_bookmarks_24
                        } else {
                            xcj.app.compose_share.R.drawable.ic_bookmarks_24
                        }

                        Icon(
                            painterResource(id = resId),
                            stringResource(xcj.app.appsets.R.string.collect_it),
                            modifier = Modifier
                                .clip(CircleShape)
                                .combinedClickableSingle(role = Role.Button) {
                                    if (viewScreenInfo.isCollectedByUser) {
                                        onCollectClick(null)
                                    } else {
                                        val bottomSheetContainerState =
                                            anyStateProvider.bottomSheetState()
                                        bottomSheetContainerState.show {
                                            CollectEditBox(
                                                onConfirmClick = {
                                                    bottomSheetContainerState.hide()
                                                }
                                            )
                                        }
                                    }
                                }
                                .padding(12.dp)
                        )
                    }
                }
                if (!LocalAccountManager.isLogged()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    DesignHDivider(modifier = Modifier.width(42.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.login_required),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

        }
    }
}

@Composable
fun CollectEditBox(
    onConfirmClick: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row {
            Text(text = stringResource(xcj.app.appsets.R.string.add_favorite_category))
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        var collectCategory by remember {
            mutableStateOf("")
        }
        DesignTextField(
            modifier = Modifier.fillMaxWidth(),
            value = collectCategory,
            onValueChange = {
                collectCategory = it
            },
            placeholder = {
                Text(text = stringResource(xcj.app.appsets.R.string.default_))
            },
            maxLines = 1,
            shape = MaterialTheme.shapes.extraLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = onConfirmClick
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.sure))
            }
        }
    }
}

@Composable
fun ScreenReviews(
    screenReviews: List<ScreenReview>?,
    onBioClick: (Bio) -> Unit
) {
    if (!screenReviews.isNullOrEmpty()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            Text(
                fontSize = 12.sp,
                text = stringResource(xcj.app.appsets.R.string.reply_below),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            for (reversedIndex in (screenReviews.size - 1 downTo 0)) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val review = screenReviews[reversedIndex]
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "#${(reversedIndex + 1)}",
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                        AnyImage(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    review.userInfo?.let { onBioClick.invoke(it) }
                                },
                            any = review.userInfo?.bioUrl
                        )
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "${review.userInfo?.name ?: ""} | ${review.reviewTime}",
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(text = review.content, fontSize = 12.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(150.dp))
        }
    } else {
        Box(
            Modifier
                .height(250.dp)
                .fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.no_reply), fontSize = 12.sp)
        }
    }
}
