@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.model.ScreenInfoForCard
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.modifier.combinedClickableSingle
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState

@Composable
fun ScreenDetailsPage(
    screenInfoForCard: ScreenInfoForCard,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onEditClick: () -> Unit,
    onCollectClick: (String?) -> Unit,
    onLikesClick: () -> Unit,
    onReviewConfirm: (String?) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onPageShowPrevious: () -> Unit,
    onPageShowNext: () -> Unit
) {
    HideNavBar()
    if (screenInfoForCard.screenInfo == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.no_corresponding_screen_found),
                modifier = Modifier.align(
                    Alignment.Center
                )
            )
            DesignBackButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }

    } else {
        var isShowLikeBigIconAnimation by remember {
            mutableStateOf(false)
        }

        val coroutineScope = rememberCoroutineScope()
        val hazeState = remember {
            HazeState()
        }
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box {
                    val backgroundImage =
                        screenInfoForCard.screenInfo.mediaFileUrls?.firstOrNull {
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
                            model = backgroundImageUrl
                        )
                    }

                    Column {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        val hapticFeedback = LocalHapticFeedback.current
                        ScreenDetailsTopBar(
                            screenInfoForCard = screenInfoForCard,
                            onBackClick = onBackClick,
                            onBioClick = onBioClick,
                            onEditClick = onEditClick,
                            onCollectClick = onCollectClick,
                            onLikesClick = {
                                onLikesClick()
                                coroutineScope.launch {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isShowLikeBigIconAnimation = true
                                }
                            }
                        )
                    }
                }
                ScreenDetailsPager(
                    screenInfoForCard = screenInfoForCard,
                    onBioClick = onBioClick,
                    onScreenMediaClick = onScreenMediaClick,
                    onPageShowPrevious = onPageShowPrevious,
                    onPageShowNext = onPageShowNext
                )
            }

            AddReviewSpace(
                modifier = Modifier.align(Alignment.BottomCenter),
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
                tint = Color.Red,
                contentDescription = stringResource(xcj.app.appsets.R.string.favorite)
            )
        }
    }
}

@Composable
fun AddReviewSpace(
    modifier: Modifier,
    onReviewConfirm: (String?) -> Unit,
) {
    var inputContentText by remember {
        mutableStateOf("")
    }
    var isShowInputContentSheet by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SuggestionChip(
            onClick = {
                isShowInputContentSheet = true
            },
            label = {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.add_reply)
                )
            },
            shape = CircleShape,
            colors = SuggestionChipDefaults.suggestionChipColors().copy(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        )
    }

    if (isShowInputContentSheet) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isShowInputContentSheet = false
            },
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.outline
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    FilledTonalButton(onClick = {
                        isShowInputContentSheet = false
                        onReviewConfirm(inputContentText)
                    }) {
                        Text(text = stringResource(xcj.app.starter.R.string.ok))
                    }
                }
                DesignTextField(
                    value = inputContentText,
                    onValueChange = {
                        inputContentText = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.text_something),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ScreenDetailsPager(
    screenInfoForCard: ScreenInfoForCard,
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
                        screenInfo = screenInfoForCard.screenInfo!!,
                        onBioClick = onBioClick,
                        pictureInteractionFlowCollector = { a, b -> },
                        onScreenMediaClick = onScreenMediaClick,
                    )
                }

            }
            DesignHDivider()
            ScreenReviews(screenInfoForCard.reviews, onBioClick)
        }
    }

}

@Composable
fun ScreenDetailsTopBar(
    screenInfoForCard: ScreenInfoForCard,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onEditClick: () -> Unit,
    onCollectClick: (String?) -> Unit,
    onLikesClick: () -> Unit,
) {
    val anyStateProvider = LocalVisibilityComposeStateProvider.current
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
                            targetState = screenInfoForCard.viewCount,
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
                            targetState = screenInfoForCard.likedCount,
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
                        LocalAccountManager.isLoggedUser(screenInfoForCard.screenInfo?.userInfo?.uid)
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
                        val resId = if (screenInfoForCard.isCollectedByUser) {
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
                                    if (screenInfoForCard.isCollectedByUser) {
                                        onCollectClick(null)
                                    } else {
                                        val bottomSheetContainerState =
                                            anyStateProvider.bottomSheetState()
                                        bottomSheetContainerState.show {
                                            CollectEditSheetContent(
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
fun CollectEditSheetContent(
    onConfirmClick: () -> Unit,
) {
    var collectCategory by remember {
        mutableStateOf("")
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.add_favorite_category))
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(
                onClick = onConfirmClick
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.ok))
            }
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
                            model = review.userInfo?.bioUrl
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
