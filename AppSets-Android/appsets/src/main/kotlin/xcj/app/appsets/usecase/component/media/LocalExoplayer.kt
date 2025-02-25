package xcj.app.appsets.usecase.component.media

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.ui.model.SpotLightState
import xcj.app.appsets.util.ktx.asContextOrNull
import xcj.app.starter.android.util.PurpleLogger

class LocalExoplayer(
    private val videoPlayerState: MutableState<SpotLightState.VideoPlayer>
) : DefaultLifecycleObserver {
    companion object {
        private const val TAG = "LocalExoplayer"
    }

    private var playWhenReady = true
    private var playerCurrentMediaItemIndex = 0
    private var playbackPosition = 0L
    private var playbackItem: MediaItem? = null
    private var exoPlayer: ExoPlayer? = null

    private fun initExoPlayer(context: Context) {
        if (exoPlayer != null) {
            return
        }
        exoPlayer = ExoPlayer.Builder(context).build().also {
            it.playWhenReady = playWhenReady
            val playId = videoPlayerState.value.playId
            if (playbackItem != null) {
                it.addMediaItem(playbackItem!!)
                if (playbackPosition != 0L) {
                    it.seekTo(playerCurrentMediaItemIndex, playbackPosition)
                }
                it.prepare()
            }
        }
        exoPlayer?.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                PurpleLogger.current.d(TAG, "onEvents")
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                PurpleLogger.current.d(TAG, "onMediaMetadataChanged:${mediaItem}")
            }

            override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                PurpleLogger.current.d(
                    TAG,
                    "onMediaMetadataChanged:${mediaMetadata}"
                )
            }

            @SuppressLint("UnsafeOptInUsageError")
            override fun onMetadata(metadata: androidx.media3.common.Metadata) {
                super.onMetadata(metadata)
                PurpleLogger.current.d(TAG, "onMetadata:${metadata}")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                PurpleLogger.current.d(
                    TAG,
                    "onPlaybackStateChanged:${playbackState}"
                )
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    /*val gson = Gson()
                    val v = gson.toJson(exoPlayer?.mediaMetadata)*/
                    PurpleLogger.current.d(
                        TAG,
                        "onPlaybackStateChanged:exoPlayer?.mediaMetadata:${exoPlayer?.mediaMetadata?.artist}"
                    )
                }

            }
        })
    }

    private fun releasePlayer() {
        exoPlayer?.run {
            playbackItem = this.currentMediaItem
            playbackPosition = this.currentPosition
            playerCurrentMediaItemIndex = this.currentMediaItemIndex
            playWhenReady = this.playWhenReady
            release()
        }
        exoPlayer = null
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        owner.asContextOrNull()?.let { context ->
            initExoPlayer(context)
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        owner.asContextOrNull()?.let { context ->
            initExoPlayer(context)
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        pause()
    }

    override fun onStop(owner: LifecycleOwner) {
        stop()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        releasePlayer()
        owner.lifecycle.removeObserver(this)
    }

    fun playVideo(
        videoUriJson: CommonURIJson,
        addToList: Boolean = false,
        playWhenReady: Boolean = true
    ) {
        PurpleLogger.current.d(
            TAG,
            """
                playVideo, 
                playerCurrentMediaItemIndex:${playerCurrentMediaItemIndex}
                playbackPosition:${playbackPosition},
                videoJson:$videoUriJson,
                exoPlayer:$exoPlayer
            """.trimIndent()
        )
        val exoPlayer = exoPlayer
        if (exoPlayer == null) {
            return
        }
        if (videoUriJson.id == videoPlayerState.value.playId
            && (exoPlayer.isPlaying || exoPlayer.isLoading)
        ) {
            return
        }
        videoPlayerState.apply {
            value = value.copy(playId = videoUriJson.id)
        }
        val mediaItem = MediaItem.fromUri(videoUriJson.uri)

        if (addToList) {
            exoPlayer.addMediaItem(mediaItem)
        } else {
            exoPlayer.setMediaItem(mediaItem)
        }
        if (playbackPosition != 0L) {
            exoPlayer.seekTo(playerCurrentMediaItemIndex, playbackPosition)
        }
        this.playWhenReady = playWhenReady
        exoPlayer.prepare()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun stop() {
        exoPlayer?.stop()
    }

    fun reset() {
        exoPlayer?.seekToDefaultPosition()
    }

    private var lastPlayerView: PlayerView? = null

    @SuppressLint("UnsafeOptInUsageError")
    fun attachPlayerView(playerView: PlayerView) {
        PurpleLogger.current.d(
            TAG,
            "attachPlayerView, exoPlayer:$exoPlayer"
        )
        val exoplayer = exoPlayer ?: return
        lastPlayerView?.player = null
        lastPlayerView = playerView
        playerView.player = exoplayer
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun detachPlayerView(playerView: PlayerView?) {
        PurpleLogger.current.d(
            TAG,
            "detachPlayerView, exoPlayer:$exoPlayer"
        )
        lastPlayerView?.player = null
        lastPlayerView = null
        playerView?.player = null
    }

    fun seekTo(w: Long) {
        exoPlayer?.seekTo(w)
    }
}