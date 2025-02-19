package xcj.app.appsets.ui.compose.media.video.single

import android.content.res.Configuration
import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import xcj.app.appsets.util.ktx.proxyTimeBar
import xcj.app.appsets.ui.compose.media.video.DesignComposedTimeBar
import xcj.app.appsets.ui.compose.media.video.fall.DesignPlayerViewProvider
import xcj.app.starter.android.ActivityThemeInterface

private const val TAG = "MediaPlaybackContent"

@OptIn(UnstableApi::class)
@Composable
fun MediaPlaybackContent() {
    val context = LocalContext.current

    val viewModel = viewModel<MediaPlaybackViewModel>()

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

    LaunchedEffect(Unit) {
        viewModel.play()
    }

    LaunchedEffect(aspectRadioState.value) {
        playerViewState.value?.resizeMode = if (aspectRadioState.value) {
            AspectRatioFrameLayout.RESIZE_MODE_FILL
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

    Box(
        Modifier
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
                viewModel.attachPlayerView(playerView)
                playerView
            }
        ) { playerView ->
            playerViewState.value = playerView
        }
    }
}