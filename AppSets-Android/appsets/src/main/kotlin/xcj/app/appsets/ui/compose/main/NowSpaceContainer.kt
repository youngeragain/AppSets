package xcj.app.appsets.ui.compose.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
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
                        )
                    }

                    is NowSpaceContent.PlatformPermissionUsageTips -> {

                        PlatformPermissionUsageTipsQuickStepBar(
                            nowSpaceContent = targetNowSpaceContent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStepBarContainer(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    onClick: () -> Unit,
    onDismissClick: () -> Unit,
    barContent: @Composable () -> Unit,
) {
    val isBarVisible by rememberUpdatedState(isVisible)
    AnimatedVisibility(
        visible = isBarVisible,
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
                    .clickable(onClick = onClick)
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
fun ImMessageQuickStepBar(
    modifier: Modifier = Modifier,
    nowSpaceContent: NowSpaceContent.IMMessage,
) {
    val context = LocalContext.current
    Row(
        modifier = modifier,
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
                label = "message_quick_access_bar_animate_0",
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { targetNewImMessage ->
                Text(text = targetNewImMessage.session.imObj.name)
            }
            AnimatedContent(
                targetState = nowSpaceContent,
                label = "message_quick_access_bar_animate_1",
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
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnyImage(
                                        modifier = Modifier
                                            .size(32.dp)
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
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            is GroupRequestJson -> {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AnyImage(
                                        modifier = Modifier
                                            .size(32.dp)
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
                                        fontSize = 12.sp
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
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PlatformPermissionUsageTipsQuickStepBar(
    modifier: Modifier = Modifier,
    nowSpaceContent: NowSpaceContent.PlatformPermissionUsageTips,
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
        }
    }
}

private fun barEnterTransition(): EnterTransition {
    return fadeIn() +
            scaleIn(
                transformOrigin = TransformOrigin.Center.copy(1f, 0.5f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) +
            slideInHorizontally(
                initialOffsetX = { it / 10 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
}

private fun barExitTransition(): ExitTransition {
    return fadeOut() +
            scaleOut(
                transformOrigin = TransformOrigin.Center.copy(1f, 0.5f),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) +
            slideOutHorizontally(
                targetOffsetX = { it / 10 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
}