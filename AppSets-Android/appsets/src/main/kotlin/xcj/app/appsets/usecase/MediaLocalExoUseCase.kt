package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.media3.ui.PlayerView
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.ui.compose.media.video.fall.VideoMediaContent
import xcj.app.appsets.ui.model.SpotLightState
import xcj.app.appsets.usecase.component.media.LocalExoplayer
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger

class MediaLocalExoUseCase() : IComposeLifecycleAware {

    companion object {
        private const val TAG = "MediaLocalExoUseCase"
    }

    private val videoPlayerState: MutableState<SpotLightState.VideoPlayer> =
        mutableStateOf(SpotLightState.VideoPlayer(playId = Int.MAX_VALUE.toString()))

    private val localExoPlayer: LocalExoplayer =
        LocalExoplayer(videoPlayerState)

    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(localExoPlayer)
    }

    fun playVideo(videoUriJson: CommonURIJson, playWhenReady: Boolean) {
        PurpleLogger.current.d(
            TAG,
            "playVideo, localExoPlayer.hashcode:${localExoPlayer.hashCode()}"
        )
        localExoPlayer.playVideo(videoUriJson, playWhenReady = playWhenReady)
    }

    fun requestStop() {
        localExoPlayer.stop()
    }

    fun prepareUriVideo(videoMediaContent: VideoMediaContent, playWhenReady: Boolean) {
        PurpleLogger.current.d(TAG, "prepareUriVideo")
        runCatching {
            val videoURLJson =
                CommonURIJson(
                    videoMediaContent.id,
                    videoMediaContent.mediaContent.name ?: "",
                    videoMediaContent.mediaContent.uri
                )
            localExoPlayer.playVideo(videoURLJson, playWhenReady = playWhenReady)
        }
    }

    fun attachPlayerView(playerView: PlayerView) {
        localExoPlayer.attachPlayerView(playerView)
    }

    fun detachPlayerView(playerView: PlayerView?) {
        localExoPlayer.detachPlayerView(playerView)
    }

    fun pause() {
        localExoPlayer.pause()
    }

    fun stop() {
        localExoPlayer.stop()
    }

    fun reset() {
        localExoPlayer.reset()
    }

    fun seekTo(w: Long) {
        localExoPlayer.seekTo(w)
    }

    override fun onComposeDispose(by: String?) {

    }
}