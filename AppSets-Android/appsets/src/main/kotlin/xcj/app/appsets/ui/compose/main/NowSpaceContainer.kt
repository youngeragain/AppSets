@file:OptIn(ExperimentalHazeMaterialsApi::class)

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
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
import xcj.app.starter.android.ktx.startWithHttpSchema

@Composable
fun NowSpaceContainer(
    modifier: Modifier = Modifier,
    navController: NavController,
    hazeState: HazeState,
) {
    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val nowSpaceContents = nowSpaceContentUseCase.contents
    QuickStepBarsContainer(
        modifier = modifier,
        isVisible = nowSpaceContents.isNotEmpty()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = nowSpaceContents,
                key = {
                    it.id
                }
            ) { nowSpaceContent ->
                QuickStepBar(
                    modifier = Modifier.animateItem(),
                    hazeState = hazeState,
                    nowSpaceContent = nowSpaceContent,
                    onClick = {
                        when (nowSpaceContent) {
                            is NowSpaceContent.AppVersionChecked -> {

                            }

                            is NowSpaceContent.IMMessage -> {
                                nowSpaceContentUseCase.removeContent(nowSpaceContent)
                                when (nowSpaceContent.message) {
                                    is SystemMessage -> {

                                        conversationUseCase.updateCurrentTab(ConversationUseCase.SYSTEM)
                                        navController.navigate(PageRouteNames.ConversationOverviewPage) {
                                            popUpTo(PageRouteNames.ConversationOverviewPage) {
                                                inclusive = true
                                            }
                                        }

                                    }

                                    else -> {
                                        conversationUseCase.updateCurrentSessionBySession(
                                            nowSpaceContent.session
                                        )
                                        navController.navigate(PageRouteNames.ConversationDetailsPage) {
                                            popUpTo(PageRouteNames.ConversationDetailsPage) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            }

                            is NowSpaceContent.PlatformPermissionUsageTips -> {
                                nowSpaceContentUseCase.removeContent(nowSpaceContent)
                                navController.navigate(PageRouteNames.PrivacyPage)
                            }
                        }
                    },
                    onLongClick = {

                    },
                    onDismissClick = {
                        nowSpaceContentUseCase.removeContent(nowSpaceContent)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickStepBarsContainer(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = barsEnterTransition(),
            exit = barsExitTransition()
        ) {
            content()
        }
    }
}


@Composable
private fun QuickStepBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    nowSpaceContent: NowSpaceContent,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    val tempNowSpaceContent by rememberUpdatedState(nowSpaceContent)
    var isBarLongPressed by remember {
        mutableStateOf(false)
    }
    Box(modifier = modifier) {
        QuickStepBarContainer(
            nowSpaceContent = tempNowSpaceContent,
            hazeState = hazeState,
            onClick = onClick,
            onLongClick = {
                isBarLongPressed = !isBarLongPressed
                onLongClick()
            },
            onDismissClick = onDismissClick
        ) {
            AnimatedContentIf(
                test = {
                    true
                },
                targetState = tempNowSpaceContent
            ) { targetNowSpaceContent ->
                when (targetNowSpaceContent) {

                    is NowSpaceContent.AppVersionChecked -> {
                        AppVersionCheckedBar(
                            nowSpaceContent = targetNowSpaceContent,
                            isBarLongPressed = isBarLongPressed,
                        )
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
    hazeState: HazeState,
    nowSpaceContent: NowSpaceContent,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDismissClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val navigationUseCase = LocalUseCaseOfNavigation.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val currentRoute by navigationUseCase.currentRouteState
    val currentSessionState by conversationUseCase.currentSessionState
    val tempNowSpaceContent by rememberUpdatedState(nowSpaceContent)
    val isBarVisible by remember {
        derivedStateOf {
            when (tempNowSpaceContent) {


                is NowSpaceContent.AppVersionChecked -> {
                    true
                }

                is NowSpaceContent.IMMessage -> {
                    val nowSpaceContentOfIMMessage =
                        tempNowSpaceContent as NowSpaceContent.IMMessage
                    val currentSession =
                        (currentSessionState as? SessionState.Normal)?.session
                    currentSession?.id != nowSpaceContentOfIMMessage.session.id ||
                            currentRoute != PageRouteNames.ConversationDetailsPage
                }

                is NowSpaceContent.PlatformPermissionUsageTips -> {
                    true
                }
            }
        }
    }

    val isDismissIconVisible by remember {
        derivedStateOf {
            when (tempNowSpaceContent) {
                is NowSpaceContent.AppVersionChecked -> {
                    val nowSpaceContentOfAppVersionChecked =
                        tempNowSpaceContent as NowSpaceContent.AppVersionChecked
                    nowSpaceContentOfAppVersionChecked.updateCheckResult.forceUpdate != true
                }

                else -> {
                    true
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isBarVisible,
        enter = barEnterTransition(),
        exit = barExitTransition()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
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
                    .hazeEffect(hazeState, HazeMaterials.thin())
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
                    content()
                }
                if (isDismissIconVisible) {
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
}

@Composable
fun AppVersionCheckedBar(
    modifier: Modifier = Modifier,
    nowSpaceContent: NowSpaceContent.AppVersionChecked,
    isBarLongPressed: Boolean,
) {
    val context = LocalContext.current
    val updateCheckResult = nowSpaceContent.updateCheckResult
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${stringResource(id = xcj.app.appsets.R.string.a_newer_version_available)}\n" +
                    "${updateCheckResult.versionFromTo}\n" +
                    "${updateCheckResult.publishDateTime}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Text(
            text = stringResource(xcj.app.appsets.R.string.version_changes),
            fontSize = 15.sp
        )
        Text(
            text = updateCheckResult.updateChangesHtml
                ?: stringResource(xcj.app.appsets.R.string.not_provided),
            fontSize = 15.sp
        )
        FilledTonalButton(
            onClick = {
                if (!updateCheckResult.downloadUrl.startWithHttpSchema()) {
                    return@FilledTonalButton
                }
                val uri =
                    updateCheckResult.downloadUrl?.toUri()
                if (uri == null) {
                    return@FilledTonalButton
                }
                navigateToExternalWeb(context, uri)
            }
        ) {
            Text(text = stringResource(xcj.app.appsets.R.string.download))
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
                        if (targetNewImMessage.message is SystemMessage) {
                            val systemContentInterface =
                                targetNewImMessage.message.systemContentInterface
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
                                targetNewImMessage.message
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

private fun barsEnterTransition(): EnterTransition {
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

private fun barsExitTransition(): ExitTransition {
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

private fun barEnterTransition(): EnterTransition {
    return fadeIn(
        animationSpec = tween()
    )
}

private fun barExitTransition(): ExitTransition {
    return fadeOut(
        animationSpec = tween()
    )
}