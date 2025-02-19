package xcj.app.appsets.ui.compose.media.video.fall

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import xcj.app.appsets.util.ktx.proxyBufferView
import xcj.app.appsets.util.ktx.removeControllerViewFor
import xcj.app.appsets.util.ktx.setControllerDrawableFor
import xcj.app.appsets.ui.compose.media.video.DesignBufferView
import xcj.app.starter.android.util.PurpleLogger

@SuppressLint("UnsafeOptInUsageError")
object DesignPlayerViewProvider {

    private const val TAG = "DesignPlayerViewProvider"

    @JvmStatic
    fun providePlayerView(context: Context): PlayerView {
        val playerView = PlayerView(context).apply {
            useController = true
            //resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            //setCustomErrorMessage(context.getString(R.string.can_not_play))
            artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_FILL
            imageDisplayMode = PlayerView.IMAGE_DISPLAY_MODE_FILL
            setKeepContentOnPlayerReset(true)
            setShowPreviousButton(false)
            setShowNextButton(false)
            setShowRewindButton(false)
            setShowFastForwardButton(false)
            //setShutterBackgroundColor(Color.TRANSPARENT)
            setAspectRatioListener(
                AspectRatioFrameLayout.AspectRatioListener { targetAspectRatio, naturalAspectRatio, aspectRatioMismatch ->
                    PurpleLogger.current.d(
                        TAG,
                        "onAspectRatioUpdated,($targetAspectRatio, $naturalAspectRatio, $aspectRatioMismatch)"
                    )
                }
            )
            setControllerDrawableFor(
                "playButtonDrawable",
                ResourcesCompat.getDrawable(
                    context.resources,
                    xcj.app.compose_share.R.drawable.ic_play_circle_filled_24,
                    null,
                )
            )
            setControllerDrawableFor(
                "pauseButtonDrawable",
                ResourcesCompat.getDrawable(
                    context.resources,
                    xcj.app.compose_share.R.drawable.ic_round_pause_circle_filled_24,
                    null,
                )
            )
            removeControllerViewFor(
                androidx.media3.ui.R.id.exo_controls_background,
                androidx.media3.ui.R.id.exo_bottom_bar,
                androidx.media3.ui.R.id.exo_minimal_controls,
            )
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            proxyBufferView(DesignBufferView(context))
        }
        playerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return playerView
    }
}