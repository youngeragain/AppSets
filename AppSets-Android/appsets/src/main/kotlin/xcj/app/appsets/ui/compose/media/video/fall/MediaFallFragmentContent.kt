package xcj.app.appsets.ui.compose.media.video.fall

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.custom_component.ImageButtonComponent
import xcj.app.appsets.ui.compose.media.video.DesignComposedTimeBar
import xcj.app.appsets.util.InstrumentationHelper
import xcj.app.appsets.util.ktx.proxyTimeBar
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "MediaFallFragmentContent"

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun MediaFallFragmentContent(
    activityViewModel: MediaFallViewModel,
    viewModel: MediaFallFragmentViewModel,
) {

    val context = LocalContext.current
    val customNightTheme = remember {
        mutableStateOf(true)
    }
    val aspectRadioState = remember {
        mutableStateOf(false)
    }

    val playerViewState = remember {
        mutableStateOf<PlayerView?>(null)
    }

    val isNightModeFromSystem =
        (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val backgroundColor = if (customNightTheme.value) {
        Color.Black
    } else {
        if (isNightModeFromSystem) {
            Color.Black
        } else {
            MaterialTheme.colorScheme.surface
        }
    }
    val bgColorState by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(350),
        label = "bg_color_animate"
    )

    LaunchedEffect(aspectRadioState.value) {
        playerViewState.value?.resizeMode = if (aspectRadioState.value) {
            AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        } else {
            AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }

    LaunchedEffect(backgroundColor) {
        if (context is ActivityThemeInterface) {
            val isLight = backgroundColor != Color.Black
            context.setupSystemBars(isLight)
        }
    }

    LaunchedEffect(viewModel.lifecycleState.value) {
        val playerView = playerViewState.value
        PurpleLogger.current.d(
            TAG,
            "LaunchedEffect, lifecycleState:${viewModel.lifecycleState.value}, ${viewModel.positionInfo}"
        )
        if (viewModel.lifecycleState.value == Lifecycle.State.RESUMED && playerView != null) {
            activityViewModel.attachPlayerView(playerView)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColorState)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = {
                val playerView = DesignPlayerViewProvider.providePlayerView(it)
                playerView.proxyTimeBar(
                    DesignComposedTimeBar(
                        it,
                        customNightTheme,
                        aspectRadioState
                    )
                )
                playerView
            }
        ) { playerView ->
            playerViewState.value = playerView
        }

        MediaContentInfoCard(
            modifier = Modifier.align(alignment = Alignment.TopCenter),
            mediaContent = viewModel.videoMediaContent.value
        )
    }
}

@Composable
fun MediaContentInfoCard(
    modifier: Modifier = Modifier,
    mediaContent: VideoMediaContent?
) {
    val context = LocalContext.current
    var miniMode = remember {
        mutableStateOf(true)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var miniModeJobState = remember {
        mutableStateOf<Job?>(null)
    }
    DisposableEffect(Unit) {
        miniModeJobState.value = scope.launch {
            delay(3000)
            miniMode.value = false
            if (mediaContent != null) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
        onDispose {
            miniModeJobState.value?.cancel()
        }
    }
    val configuration = LocalConfiguration.current
    val overrideModifier = if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .displayCutoutPadding()
    } else {
        modifier
            .width(TextFieldDefaults.MinWidth * 2)
            .statusBarsPadding()
            .displayCutoutPadding()
    }
    Box(
        modifier = overrideModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .shadow(24.dp, MaterialTheme.shapes.extraLarge)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            miniModeJobState.value?.cancel()
                            miniMode.value = !miniMode.value
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = 0.55f,
                            stiffness = 180f,
                        ),
                        alignment = Alignment.Center
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.outline, CircleShape)
                        .width(52.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                )
                if (!miniMode.value && mediaContent != null) {
                    Column(
                        modifier = modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_outline_remove_red_eye_24),
                                contentDescription = "views",
                                modifier = Modifier.size(14.dp)
                            )
                            var viewsText = "${mediaContent.mediaContent.views}"
                            AnimatedContent(
                                targetState = viewsText,
                                transitionSpec = {
                                    fadeIn(tween()) togetherWith fadeOut(tween())
                                },
                                contentAlignment = Alignment.Center
                            ) { targetViewsText ->
                                Text(
                                    text = targetViewsText,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.animateContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    InstrumentationHelper.invoke(
                                        context,
                                        InstrumentationHelper.BACK_CLICK
                                    )
                                }
                            ) {
                                Text(
                                    text = stringResource(xcj.app.appsets.R.string.take_a_look),
                                    fontSize = 10.sp
                                )
                            }
                            FilledTonalButton(
                                onClick = {
                                    InstrumentationHelper.invoke(
                                        context,
                                        InstrumentationHelper.BACK_CLICK
                                    )
                                }
                            ) {
                                Text(
                                    text = stringResource(xcj.app.appsets.R.string.share),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            ImageButtonComponent(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        MaterialTheme.shapes.extraLarge
                                    ),
                                onClick = {
                                    InstrumentationHelper.invoke(
                                        context,
                                        InstrumentationHelper.BACK_CLICK
                                    )
                                },
                                resource = mediaContent.mediaContent.relateUser?.avatarUrl
                            )
                        }
                    }
                }
            }
        }
    }
}