@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.model.ScreenInfoForCard
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.modifier.combinedClickableSingle
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState

@OptIn(ExperimentalLayoutApi::class)
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

        val hazeState = remember {
            HazeState()
        }
        val coroutineScope = rememberCoroutineScope()
        val likeIconAnimationState = remember {
            AnimationState(0f)
        }

        var isShowScreenReviews by remember {
            mutableStateOf(false)
        }

        var isShowInputContentSheet by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(isShowLikeBigIconAnimation) {
            if (isShowLikeBigIconAnimation) {
                launch {
                    likeIconAnimationState.animateTo(1f, animationSpec = tween(450))
                }
            } else {
                launch {
                    likeIconAnimationState.animateTo(0f, animationSpec = tween(450))
                }
            }
        }
        VerticalOverscrollBox {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .hazeSourceIfAvailable(hazeState)
            ) {
                StatusBarWithTopActionBarSpacer()
                ScreenDetailsContent(
                    screenInfoForCard = screenInfoForCard,
                    onBioClick = onBioClick,
                    onScreenMediaClick = onScreenMediaClick,
                    onPageShowPrevious = onPageShowPrevious,
                    onPageShowNext = onPageShowNext
                )
            }

            AnimatedVisibility(
                visible = isShowScreenReviews,
                enter = fadeIn() + expandIn(expandFrom = Alignment.TopCenter),
                exit = shrinkOut(shrinkTowards = Alignment.TopCenter) + fadeOut()
            ) {
                ScreenReviews(
                    screenReviews = screenInfoForCard.reviews,
                    onBioClick = onBioClick,
                    onShowAddReviewClick = {
                        isShowInputContentSheet = true
                    }
                )
            }

            AddReviewSpace(
                isShowInputContentSheet = isShowInputContentSheet,
                onDismissRequest = {
                    isShowInputContentSheet = false
                },
                onReviewConfirm = onReviewConfirm
            )

            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
                    .graphicsLayer {
                        val likeIconAnimationStateValue = likeIconAnimationState.value
                        scaleX = likeIconAnimationStateValue
                        scaleY = likeIconAnimationStateValue
                        rotationY = if (isShowLikeBigIconAnimation) {
                            90 * likeIconAnimationStateValue
                        } else {
                            90 * (2 - likeIconAnimationStateValue)
                        }
                    },
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_favorite_24),
                tint = Color.Red,
                contentDescription = stringResource(xcj.app.appsets.R.string.favorite)
            )

            BackActionTopBar(
                hazeState = hazeState,
                onBackClick = onBackClick,
                customEndContent = {
                    ScreenDetailsTopEndActions(
                        screenInfoForCard = screenInfoForCard,
                        onLikesClick = {
                            onLikesClick()
                            coroutineScope.launch {
                                isShowLikeBigIconAnimation = true
                                delay(450)
                                isShowLikeBigIconAnimation = false
                            }
                        },
                        onEditClick = onEditClick,
                        onCollectClick = onCollectClick,
                        onReviewClick = {
                            isShowScreenReviews = !isShowScreenReviews
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun ScreenDetailsTopEndActions(
    screenInfoForCard: ScreenInfoForCard,
    onLikesClick: () -> Unit,
    onEditClick: () -> Unit,
    onCollectClick: (String?) -> Unit,
    onReviewClick: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current

    Row(modifier = Modifier.padding(horizontal = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(xcj.app.compose_share.R.drawable.ic_notes_24),
                stringResource(xcj.app.appsets.R.string.reply),
                modifier = Modifier
                    .clip(CircleShape)
                    .combinedClickableSingle(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onReviewClick()
                    })
                    .padding(12.dp)
            )
        }

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
                painterResource(xcj.app.compose_share.R.drawable.ic_outline_remove_red_eye_24),
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
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (screenInfoForCard.isCollectedByUser) {
                            onCollectClick(null)
                        } else {
                            val bottomSheetState =
                                visibilityComposeStateProvider.bottomSheetState()
                            bottomSheetState.show(null) {
                                CollectEditSheetContent(
                                    onConfirmClick = {
                                        bottomSheetState.hide()
                                    }
                                )
                            }
                        }
                    }
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun AddReviewSpace(
    isShowInputContentSheet: Boolean,
    onDismissRequest: () -> Unit,
    onReviewConfirm: (String?) -> Unit,
) {
    var inputContentText by remember {
        mutableStateOf("")
    }

    if (isShowInputContentSheet) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = onDismissRequest,
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
                        onDismissRequest()
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
fun ScreenDetailsContent(
    modifier: Modifier = Modifier,
    screenInfoForCard: ScreenInfoForCard,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onPageShowPrevious: () -> Unit,
    onPageShowNext: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SelectionContainer(
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Screen(
                pageRouteName = PageRouteNames.ScreenDetailsPage,
                screenInfo = screenInfoForCard.screenInfo!!,
                onBioClick = onBioClick,
                onScreenMediaClick = onScreenMediaClick,
            )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScreenReviews(
    screenReviews: List<ScreenReview>?,
    onBioClick: (Bio) -> Unit,
    onShowAddReviewClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            if (!screenReviews.isNullOrEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 68.dp,
                        bottom = 150.dp
                    )
                )
                {
                    itemsIndexed(items = screenReviews.reversed()) { index, review ->
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        )
                        {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "#${(index + 1)}",
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
                                    text = "${review.userInfo?.bioName ?: ""} | ${review.reviewTime}",
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
                }
            } else {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.no_reply),
                    fontSize = 12.sp,
                    modifier = Modifier.align(
                        Alignment.Center
                    )
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = 12.dp + WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues()
                            .calculateBottomPadding()
                    )
            ) {
                FilledTonalButton(
                    onClick = onShowAddReviewClick
                ) {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.add_reply)
                    )
                }
            }
        }
    }
}
