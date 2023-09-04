package xcj.app.appsets.usecase

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import xcj.app.appsets.im.CommonURLJson
import xcj.app.appsets.service.MediaPlayback101Service
import xcj.app.appsets.ui.compose.win11Snapshot.SpotLightState
import xcj.app.appsets.util.DesignRecorder
import xcj.app.core.android.toplevelfun.timestampToMSS
import xcj.app.core.foundation.usecase.NoConfigUseCase
import java.io.File
import java.lang.ref.WeakReference

interface LifecycleAwareForPlayer {

    fun onCreate(context: Context)
    fun onStart(context: Context)
    fun onResume(context: Context)
    fun onPause(context: Context)
    fun onStop(context: Context)
    fun onDestroy(context: Context)
}

/**
 * @param componentsToUsed remotePlayer, localPlayer
 */
@UnstableApi
class MediaUseCase(
    private val componentsToUsed: Array<String>,
    private val initLocalOnStartOrResume: Boolean
) : NoConfigUseCase(), LifecycleAwareForPlayer {

    val audioPlayerState: MutableState<SpotLightState.AudioPlayer?> = mutableStateOf(null)
    val videoPlayerState: MutableState<SpotLightState.VideoPlayer?> = mutableStateOf(null)
    private var remoteExoplayer: RemoteExoplayer? = null
    private var localExoPlayer: LocalExoplayer? = null

    override fun onCreate(context: Context) {
        Log.i(
            "MediaUseCase",
            "onCreate hashcode:${hashCode()}, remoteExoplayer:${remoteExoplayer}, localExoPlayer:${localExoPlayer}"
        )
        if (componentsToUsed.contains("local")) {
            if (localExoPlayer != null)
                return
            localExoPlayer = LocalExoplayer()
            localExoPlayer?.initOnStartOrResume = initLocalOnStartOrResume
            localExoPlayer?.videoPlayerState = videoPlayerState
            localExoPlayer?.onCreate(context)
        }
        if (componentsToUsed.contains("remote")) {
            if (remoteExoplayer != null)
                return
            remoteExoplayer = RemoteExoplayer()
            remoteExoplayer?.audioPlayerState = audioPlayerState
            remoteExoplayer?.onCreate(context)
        }
    }

    override fun onStart(context: Context) {
        localExoPlayer?.onStart(context)
        remoteExoplayer?.onStart(context)
    }

    override fun onResume(context: Context) {
        localExoPlayer?.onResume(context)
        remoteExoplayer?.onResume(context)
    }

    override fun onPause(context: Context) {
        localExoPlayer?.onPause(context)
        remoteExoplayer?.onPause(context)
    }

    override fun onStop(context: Context) {
        localExoPlayer?.onStop(context)
        remoteExoplayer?.onStop(context)
        stopRecord(context, "lifecycle stop")
    }

    override fun onDestroy(context: Context) {
        localExoPlayer?.onDestroy(context)
        remoteExoplayer?.onDestroy(context)
    }

    private var designRecorder: DesignRecorder? = null
    val recorderState: MutableState<Pair<Boolean, Int>?> = mutableStateOf(null)
    fun startRecord(context: Context) {
        if (designRecorder == null) {
            designRecorder = DesignRecorder()
            val outPut = DesignRecorder.OutPut().apply {
                outDirFile = File(context.cacheDir.path + "/audio_record/")
            }
            designRecorder!!.setOutPut(outPut)
            designRecorder!!.setUpdateCallback(object : DesignRecorder.UpdateCallBack {
                override fun onUpdate(startTime: Long, seconds: Int) {
                    recorderState.value = true to seconds
                }

                override fun onStop(type: Int) {
                    recorderState.value = null
                }
            })
        }
        designRecorder!!.startRecord(context)
    }

    fun getRecordFiles(): List<String>? {
        return designRecorder?.getRecordFiles()
    }

    fun stopRecord(context: Context, by: String) {
        designRecorder?.stopRecord()
    }

    fun pauseAudio() {
        remoteExoplayer?.pauseAudio()
    }

    fun playAudio(id: String, musicJson: CommonURLJson.MusicURLJson) {
        localExoPlayer?.pause()
        remoteExoplayer?.playAudio(id, musicJson)
    }

    fun playVideo(id: String, videoJson: CommonURLJson.VideoURLJson) {
        remoteExoplayer?.pauseAudio()
        Log.i("MediaUseCase", "playVideo, localExoPlayer.hashcode:${localExoPlayer?.hashCode()}")
        localExoPlayer?.playVideo(id, videoJson)
    }

    fun playWith(playerView: PlayerView) {
        localExoPlayer?.playWith(playerView)
    }


    @UnstableApi
    class RemoteExoplayer : LifecycleAwareForPlayer {
        private val TAG = "RemoteExoplayer"
        private var mediaBrowser: MediaBrowserCompat? = null
        private var connectionCallbacks: MediaBrowserCompat.ConnectionCallback? = null

        private var mediaController: MediaControllerCompat? = null
        private var controllerCallback: MediaControllerCompat.Callback? = null

        lateinit var audioPlayerState: MutableState<SpotLightState.AudioPlayer?>
        private fun createMediaBrowser(context: Context) {
            if (mediaBrowser != null)
                return
            connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {

                    // Get the token for the MediaSession
                    // Create a MediaControllerCompat
                    mediaController = MediaControllerCompat(
                        context, // Context
                        mediaBrowser!!.sessionToken
                    )

                    // Save the controller
                    /*MediaControllerCompat.setMediaController(context as AppCompatActivity, mediaController)*/
                    controllerCallback = object : MediaControllerCompat.Callback() {

                        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                            //currentMediaMetadataState.value = metadata
                            Log.i(TAG, "onMetadataChanged")
                            updateAudioMetadata(metadata)
                        }

                        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                            Log.i(TAG, "onPlaybackStateChanged")
                            updateAudioPlaybackState(state)

                        }
                    }
                    // Finish building the UI
                    mediaController?.registerCallback(controllerCallback!!)
                    mediaController?.sendCommand("init_exoplayer", null, null)
                }

                override fun onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                override fun onConnectionFailed() {
                    // The Service has refused our connection
                }
            }
            mediaBrowser = MediaBrowserCompat(
                context,
                ComponentName(context, MediaPlayback101Service::class.java),
                connectionCallbacks,
                null // optional Bundle
            )
        }

        private fun connectToBrowserService() {
            Log.i(
                TAG,
                "connectToBrowserService, mediaBrowser?.isConnected:${mediaBrowser?.isConnected}"
            )
            if (mediaBrowser?.isConnected == true) {
                return
            }
            try {
                mediaBrowser?.connect()
            } catch (e: IllegalStateException) {
                //connect() called while neither disconnecting nor disconnected (state=CONNECT_STATE_CONNECTING)
                e.printStackTrace()
            }
        }

        fun updateAudioMetadata(metadata: MediaMetadataCompat?) {
            val playerState = audioPlayerState.value
            if (playerState != null) {
                audioPlayerState.value = playerState.apply {
                    mediaMetadataCompat = metadata
                    title = metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE) ?: ""
                    art = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                    duration =
                        timestampToMSS(metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: -1)
                    val position = playbackStateCompat?.position ?: 0L
                    if (playbackStateCompat?.state == PlaybackStateCompat.STATE_PLAYING) {
                        val timeDelta =
                            SystemClock.elapsedRealtime() - (playbackStateCompat?.lastPositionUpdateTime
                                ?: 0L)
                        (position + (timeDelta * (playbackStateCompat?.playbackSpeed
                            ?: 0f))).toLong()
                    }
                    currentDuration = timestampToMSS(position)
                }
            } else {
                audioPlayerState.value = SpotLightState.AudioPlayer().apply {
                    playbackStateCompat = PlaybackStateCompat.Builder().build()
                    mediaMetadataCompat = metadata
                    title = metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE) ?: ""
                    art = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                    duration =
                        timestampToMSS(metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: -1)
                    currentDuration = "00:00"
                }
            }

        }

        fun updateAudioPlaybackState(state: PlaybackStateCompat?) {
            if (state?.state != PlaybackStateCompat.STATE_PLAYING) {
                audioPlayerState.value = null
            }
            val playerState = audioPlayerState.value
            if (playerState != null) {
                audioPlayerState.value = playerState.apply {
                    playbackStateCompat = state
                }
            } else {
                audioPlayerState.value = SpotLightState.AudioPlayer().apply {
                    playbackStateCompat = state
                    mediaMetadataCompat = MediaMetadataCompat.Builder().build()
                    title = ""
                    art = ""
                    duration = "00:00"
                    currentDuration = "00:00"
                }
            }
        }


        fun playAudio(id: String, mediaJson: CommonURLJson.MediaJson) {
            if (mediaController == null)
                return
            if (audioPlayerState.value?.id != id) {//new audio to play
                val commandBundle = Bundle().apply {
                    putString("url", mediaJson.url)
                }
                mediaController!!.sendCommand("set_playback_item", commandBundle, null)
                val mediaMetadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaJson.name)
                    .putString("id", id)
                    .build()
                audioPlayerState.value = SpotLightState.AudioPlayer().apply {
                    this.id = id
                    mediaMetadataCompat = mediaMetadata
                }
            } else {//same audio
                mediaController?.transportControls?.play()
            }
        }

        fun pauseAudio() {
            mediaController?.transportControls?.pause()
        }

        fun stopAudio() {
            mediaController?.transportControls?.stop()
        }

        fun release() {
            if (mediaController == null)
                return
            if (connectionCallbacks == null)
                return
            mediaController!!.unregisterCallback(controllerCallback!!)
            controllerCallback = null
            mediaController = null
            mediaBrowser?.disconnect()
            mediaBrowser = null
        }

        override fun onCreate(context: Context) {
            createMediaBrowser(context)
        }

        override fun onStart(context: Context) {
            if (Util.SDK_INT >= 24) {
                connectToBrowserService()
            }
        }

        override fun onResume(context: Context) {
            if ((Util.SDK_INT < 24)) {
                connectToBrowserService()
            }
        }

        override fun onPause(context: Context) {
            /*if (Util.SDK_INT < 24) {
                release()
            }*/
        }

        override fun onStop(context: Context) {
            /*if (Util.SDK_INT >= 24) {
                release()
            }*/
        }

        override fun onDestroy(context: Context) {

        }
    }

    @UnstableApi
    class LocalExoplayer : LifecycleAwareForPlayer {
        private val TAG = "LocalExoplayer"
        private var playWhenReady = true
        private var currentWindow = 0
        private var playbackPosition = 0L
        private var playbackItem: MediaItem? = null
        private var exoPlayer: ExoPlayer? = null
        private var playId: String? = null
        private var playerViewWeak: WeakReference<PlayerView>? = null
        lateinit var videoPlayerState: MutableState<SpotLightState.VideoPlayer?>

        var initOnStartOrResume: Boolean = false
        private fun initExoPlayer(context: Context) {
            exoPlayer = ExoPlayer.Builder(context).build().also {
                playerViewWeak?.get()?.player = it
                it.playWhenReady = playWhenReady
                if (playId != null && playbackItem != null) {
                    it.addMediaItem(playbackItem!!)
                    if (playbackPosition != 0L)
                        it.seekTo(currentWindow, playbackPosition)
                    it.prepare()
                }
            }
            exoPlayer?.addListener(object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    Log.i(TAG, "onEvents")
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    super.onMediaItemTransition(mediaItem, reason)
                    Log.i(TAG, "onMediaMetadataChanged:${mediaItem}")
                }

                override fun onMediaMetadataChanged(mediaMetadata: androidx.media3.common.MediaMetadata) {
                    super.onMediaMetadataChanged(mediaMetadata)
                    Log.i(TAG, "onMediaMetadataChanged:${mediaMetadata}")
                }

                override fun onMetadata(metadata: androidx.media3.common.Metadata) {
                    super.onMetadata(metadata)
                    Log.i(TAG, "onMetadata:${metadata}")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    Log.i(TAG, "onPlaybackStateChanged:${playbackState}")
                    if (playbackState == Player.STATE_READY && playWhenReady) {
                        /*val gson = Gson()
                        val v = gson.toJson(exoPlayer?.mediaMetadata)*/
                        Log.i(
                            TAG,
                            "onPlaybackStateChanged:exoPlayer?.mediaMetadata:${exoPlayer?.mediaMetadata?.artist}"
                        )
                    }

                }
            })
        }

        fun releasePlayer() {
            exoPlayer?.run {
                playbackItem = this.currentMediaItem
                playbackPosition = this.currentPosition
                currentWindow = this.currentWindowIndex
                playWhenReady = this.playWhenReady
                release()
                playerViewWeak?.get()?.player = null
            }
            exoPlayer = null
        }

        @SuppressLint("InlinedApi")
        private fun hideSystemUi(view: View?) {
            view?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }

        override fun onCreate(context: Context) {

        }

        override fun onStart(context: Context) {
            if (!initOnStartOrResume)
                return
            if (Util.SDK_INT >= 24) {
                initExoPlayer(context)
            }
        }

        override fun onResume(context: Context) {
            if (!initOnStartOrResume)
                return
            if ((Util.SDK_INT < 24)) {
                initExoPlayer(context)
            }
        }

        override fun onPause(context: Context) {
            if (Util.SDK_INT < 24) {
                releasePlayer()
            }
        }

        override fun onStop(context: Context) {
            if (Util.SDK_INT >= 24) {
                releasePlayer()
            }
        }

        override fun onDestroy(context: Context) {
            playerViewWeak?.clear()
            playerViewWeak = null
        }

        fun playVideo(
            id: String,
            videoJson: CommonURLJson.VideoURLJson,
            addToList: Boolean = false
        ) {
            Log.e(
                TAG,
                "playVideo, currentWindow:${currentWindow}, playbackPosition:${playbackPosition}"
            )
            if (id == playId) {
                if (exoPlayer?.isPlaying == true || exoPlayer?.isLoading == true)
                    return
            }
            playId = id
            exoPlayer?.apply {
                val mediaItem = MediaItem.fromUri(videoJson.url)
                if (addToList)
                    addMediaItem(mediaItem)
                else {
                    setMediaItem(mediaItem)
                }
                if (playbackPosition != 0L)
                    seekTo(currentWindow, playbackPosition)
                prepare()
            }
        }

        fun pause() {
            exoPlayer?.release()
        }

        fun playWith(playerView: PlayerView) {
            exoPlayer?.let {
                if (playerView == playerViewWeak?.get())
                    return
                playerView.player = it
                playerViewWeak = WeakReference(playerView)
            }
        }
    }

}