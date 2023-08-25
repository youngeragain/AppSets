package xcj.app.appsets.ui.compose

import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.updateLayoutParams
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.compose.theme.LightColorPalette
import xcj.app.appsets.ui.nonecompose.base.BaseActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.core.android.toplevelfun.dp2px


@UnstableApi
class ExoPlayerActivity :
    BaseActivity<ViewDataBinding, ExoPlayerActivityViewModel, BaseViewModelFactory<ExoPlayerActivityViewModel>>() {

    override fun getSystemBarsHiddenState(): Pair<Boolean, Boolean> {
        return true to true
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("ExoPlayerActivity", "onCreate")
        viewModel?.initVideoJson(intent.getStringExtra("video_json"))
        viewModel?.mediaUseCase?.onCreate(this)
        setContent {
            AppSetsTheme {
                SingleExoPlayer()
            }
        }
    }

    override fun createViewModel(): ExoPlayerActivityViewModel? {
        return ViewModelProvider(this)[ExoPlayerActivityViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        viewModel?.mediaUseCase?.onStart(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel?.mediaUseCase?.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel?.mediaUseCase?.onPause(this)
    }

    override fun onStop() {
        super.onStop()
        viewModel?.mediaUseCase?.onStop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel?.mediaUseCase?.onDestroy(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("ExoPlayerActivity", "onConfigurationChanged")
        val userCustomUiMode = viewModel?.userCustomUiModel?.value
        if (userCustomUiMode != null) {
            val newIsNightModeFromSystem =
                (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            if (newIsNightModeFromSystem != userCustomUiMode) {
                viewModel?.userCustomUiModel?.value = null
            }
        }
    }
}

@UnstableApi
@Composable
fun SingleExoPlayer() {
    val context = LocalContext.current
    val viewModel = viewModel<ExoPlayerActivityViewModel>(context as AppCompatActivity)
    val isNightModeFromSystem =
        (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val backgroundColor = if (viewModel.userCustomUiModel.value != null) {
        if (viewModel.userCustomUiModel.value == true) {
            androidx.compose.ui.graphics.Color.Black
            //DarkColorPalette.background
        } else {
            LightColorPalette.secondaryContainer
        }
    } else {
        if (isNightModeFromSystem) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
    }
    val bgColorState by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(350),
        label = "bgcolor"
    )
    var isControllerViewVisible by remember {
        mutableStateOf(false)
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(bgColorState)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = {
                    val playerView = PlayerView(context).apply {
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT
                        )
                        setShowPreviousButton(false)
                        setShowNextButton(false)
                        setShowRewindButton(false)
                        setShowFastForwardButton(false)
                        controllerViewSize(
                            androidx.media3.ui.R.id.exo_play_pause,
                            dp2px(100f).toInt()
                        )
                        bottomControllerViewBackgroundColor(Color.TRANSPARENT)
                        bottomControllerViewVisibility(
                            androidx.media3.ui.R.id.exo_settings,
                            View.GONE
                        )

                        setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
                            isControllerViewVisible = visibility == View.VISIBLE
                        })

                    }
                    Log.e("SingleExoPlayer", "AndroidView->factory")
                    viewModel.playWith(playerView)
                    playerView
                }, modifier = Modifier.fillMaxSize()
            ) {
                Log.e("SingleExoPlayer", "onUpdate")
            }
        }
        AnimatedVisibility(
            visible = isControllerViewVisible,
            exit = slideOutVertically() + fadeOut(animationSpec = tween(100)),
            enter = slideInVertically() + fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(Modifier.padding(horizontal = 12.dp)) {
                        val backPressDispatcher =
                            (LocalContext.current as AppCompatActivity).onBackPressedDispatcher
                        Box(
                            modifier = Modifier
                                .size(width = 78.dp, height = 42.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = backPressDispatcher::onBackPressed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_round_arrow_24),
                                contentDescription = "back"
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 78.dp, height = 42.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = viewModel::userCustomUiMode),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconId = if (viewModel.userCustomUiModel.value != null) {
                                if (viewModel.userCustomUiModel.value == true) {
                                    R.drawable.outline_light_mode_24
                                } else {
                                    R.drawable.outline_nightlight_24
                                }
                            } else {
                                if (isNightModeFromSystem) {
                                    R.drawable.outline_light_mode_24
                                } else {
                                    R.drawable.outline_nightlight_24
                                }
                            }
                            Icon(
                                painter = painterResource(id = iconId),
                                contentDescription = "change ui mode"
                            )
                        }
                    }
                }
            }
        }
        LaunchedEffect(key1 = true, block = {
            viewModel.play()
        })

    }

}

@UnstableApi
@Composable
fun ExoPlayerList() {
    val currentContext = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(currentContext).build().apply {
            val dataSourceFactory = DefaultDataSource.Factory(
                currentContext,
                DefaultDataSource.Factory(currentContext)
            )
            val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse("https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-lavf-56.36.100-aac-avc-main-1280x720.mkv")))
            setMediaSource(source)
            prepare()
            playWhenReady = true
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    SingleExoPlayer()
}


fun PlayerView.bottomControllerViewBackgroundColor(color: Int) {
    findViewById<View>(androidx.media3.ui.R.id.exo_bottom_bar).setBackgroundColor(
        Color.TRANSPARENT
    )
}

fun PlayerView.controllerViewSize(viewId: Int, size: Int) {
    findViewById<View>(viewId).updateLayoutParams<ViewGroup.LayoutParams> {
        width = size
        height = size
    }
}

fun PlayerView.controllerImageButtonResource(viewId: Int, resourceId: Int) {
    findViewById<ImageButton>(viewId).setImageResource(resourceId)
}

@UnstableApi
fun PlayerView.bottomControllerViewVisibility(viewId: Int, visibility: Int) {
    try {
        val controllerField = PlayerView::class.java.getDeclaredField("controller")
        if (!controllerField.isAccessible) {
            controllerField.isAccessible = true
        }
        val controlView = controllerField.get(this) as PlayerControlView

        val controlViewLayoutManagerField =
            PlayerControlView::class.java.getDeclaredField("controlViewLayoutManager")
        if (!controlViewLayoutManagerField.isAccessible) {
            controlViewLayoutManagerField.isAccessible = true
        }
        val playerControlViewLayoutManager = controlViewLayoutManagerField.get(controlView)
        val showButtonMethod =
            playerControlViewLayoutManager::class.java.declaredMethods.firstOrNull {
                it.parameterTypes.size == 2 && it.parameterTypes[0] == View::class.java && it.parameterTypes[1] == Boolean::class.java
            }
        val exoSettingsButton = controlView.findViewById<View>(viewId)
        showButtonMethod?.invoke(
            playerControlViewLayoutManager,
            exoSettingsButton,
            visibility == View.VISIBLE
        )
    } catch (e: Exception) {
        Log.e("ExoPlayerActivity", "bottomControllerViewVisibility exception:${e}")
        e.printStackTrace()
    }
}