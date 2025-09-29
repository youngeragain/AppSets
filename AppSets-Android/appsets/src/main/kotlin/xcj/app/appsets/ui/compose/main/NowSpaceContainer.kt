package xcj.app.appsets.ui.compose.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNowSpaceContent
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.compose_extensions.AnimatedContentIf
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.SessionState

@Composable
fun NowSpaceContainer(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val navigationUseCase = LocalUseCaseOfNavigation.current
    val currentRoute by navigationUseCase.currentRouteState
    val nowSpaceContent by nowSpaceContentUseCase.content
    val currentSessionState by conversationUseCase.currentSessionState
    val isBarVisible by remember {
        derivedStateOf {
            val tempNowSpaceContent = nowSpaceContent
            when (tempNowSpaceContent) {
                is NowSpaceContent.Nothing -> {
                    false
                }

                is NowSpaceContent.IMMessage -> {
                    val currentSession =
                        (currentSessionState as? SessionState.Normal)?.session
                    currentSession?.id != tempNowSpaceContent.session.id ||
                            currentRoute != PageRouteNames.ConversationDetailsPage
                }

                is NowSpaceContent.PlatformPermissionUsageTips -> {
                    true
                }
            }
        }
    }
    var isBarLongPressed by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(isBarVisible) {
        if (isBarVisible) {
            isBarLongPressed = false
        }
    }
    Box(
        modifier = modifier
    ) {
        QuickStepBarContainer(
            isVisible = isBarVisible,
            onClick = {
                when (nowSpaceContent) {
                    is NowSpaceContent.Nothing -> {

                    }

                    is NowSpaceContent.IMMessage -> {
                        val imMessage = nowSpaceContent as NowSpaceContent.IMMessage
                        when (imMessage.imMessage) {
                            is SystemMessage -> {
                                nowSpaceContentUseCase.removeContent()
                                conversationUseCase.updateCurrentTab(ConversationUseCase.SYSTEM)
                                navController.navigate(PageRouteNames.ConversationOverviewPage) {
                                    popUpTo(PageRouteNames.ConversationOverviewPage) {
                                        inclusive = true
                                    }
                                }

                            }

                            else -> {
                                nowSpaceContentUseCase.removeContent()
                                conversationUseCase.updateCurrentSessionBySession(imMessage.session)
                                navController.navigate(PageRouteNames.ConversationDetailsPage) {
                                    popUpTo(PageRouteNames.ConversationDetailsPage) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                    }

                    is NowSpaceContent.PlatformPermissionUsageTips -> {
                        nowSpaceContentUseCase.removeContent()
                        navController.navigate(PageRouteNames.PrivacyPage)
                    }
                }
            },
            onLongClick = {
                isBarLongPressed = !isBarLongPressed
            },
            onDismissClick = {
                nowSpaceContentUseCase.removeContent()
            }
        ) {
            AnimatedContentIf(
                test = {
                    !nowSpaceContentUseCase.contentTypeIsSameAsLast()
                },
                targetState = nowSpaceContent
            ) { targetNowSpaceContent ->
                when (targetNowSpaceContent) {
                    is NowSpaceContent.Nothing -> {

                    }

                    is NowSpaceContent.IMMessage -> {
                        ImMessageQuickStepBar(
                            nowSpaceContent = targetNowSpaceContent,
                            isBarLongPressed = isBarLongPressed,
                        )
                    }

                    is NowSpaceContent.PlatformPermissionUsageTips -> {

                        PlatformPermissionUsageTipsQuickStepBar(
                            nowSpaceContent = targetNowSpaceContent,
                            isBarLongPressed = isBarLongPressed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStepBarContainer(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissClick: () -> Unit,
    barContent: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = barEnterTransition(),
        exit = barExitTransition()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.extraLarge
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    barContent()
                }
                Icon(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.outlineVariant,
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onDismissClick)
                        .padding(12.dp),
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_close_24),
                    contentDescription = stringResource(xcj.app.appsets.R.string.close),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ImMessageQuickStepBar(
    modifier: Modifier = Modifier,
    nowSpaceContent: NowSpaceContent.IMMessage,
    isBarLongPressed: Boolean,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier.animateContentSize(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedContent(
                targetState = nowSpaceContent,
                label = "message_quick_access_bar_animate_0",
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { targetNewImMessage ->
                AnyImage(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        ),
                    model = targetNewImMessage.session.imObj.avatarUrl
                )
            }
            Column {
                AnimatedContent(
                    targetState = nowSpaceContent,
                    label = "message_quick_access_bar_animate_1",
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    }
                ) { targetNewImMessage ->
                    Text(text = targetNewImMessage.session.imObj.name)
                }
                AnimatedContent(
                    targetState = nowSpaceContent,
                    label = "message_quick_access_bar_animate_2",
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    }
                ) { targetNewImMessage ->
                    Column {
                        if (targetNewImMessage.imMessage is SystemMessage) {
                            val systemContentInterface =
                                targetNewImMessage.imMessage.systemContentInterface
                            when (systemContentInterface) {
                                is FriendRequestJson -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AnyImage(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(MaterialTheme.shapes.extraLarge)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    MaterialTheme.shapes.extraLarge
                                                ),
                                            model = systemContentInterface.avatarUrl
                                        )
                                        Text(
                                            text = systemContentInterface.name ?: ""
                                        )
                                    }
                                }

                                is GroupRequestJson -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AnyImage(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(MaterialTheme.shapes.extraLarge)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    MaterialTheme.shapes.extraLarge
                                                ),
                                            model = systemContentInterface.avatarUrl
                                        )
                                        Text(
                                            text = systemContentInterface.name ?: "",
                                        )
                                    }
                                }

                                else -> Unit
                            }
                        }
                        Text(
                            text = IMMessage.readableContent(
                                context,
                                targetNewImMessage.imMessage
                            )
                                ?: "",
                            maxLines = if (isBarLongPressed) {
                                20
                            } else {
                                2
                            },
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformPermissionUsageTipsQuickStepBar(
    modifier: Modifier = Modifier,
    nowSpaceContent: NowSpaceContent.PlatformPermissionUsageTips,
    isBarLongPressed: Boolean,
) {
    Column(
        modifier = modifier.animateContentSize(),
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnyImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    ),
                model = xcj.app.compose_share.R.drawable.ic_info_24
            )
            Column {
                Text(
                    text = stringResource(nowSpaceContent.tips),
                )
                Text(
                    text = stringResource(nowSpaceContent.subTips),
                    fontSize = 12.sp
                )
                if (isBarLongPressed) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(
                            items = nowSpaceContent.platformPermissionsUsages,
                            key = { it.name }) { platformPermissionsUsage ->
                            Column {
                                Text(
                                    text = stringResource(platformPermissionsUsage.name),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = stringResource(platformPermissionsUsage.description),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

    }

}

private fun barEnterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween()
    ) +
            scaleIn(
                transformOrigin = TransformOrigin.Center.copy(0.5f, 0f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) +
            slideInVertically(
                initialOffsetY = { -it / 2 },
                animationSpec = tween()
            )
}

private fun barExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween()
    ) +
            scaleOut(
                transformOrigin = TransformOrigin.Center.copy(0.5f, 0f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) +
            slideOutVertically(
                targetOffsetY = { -it / 2 },
                animationSpec = tween()
            )
}