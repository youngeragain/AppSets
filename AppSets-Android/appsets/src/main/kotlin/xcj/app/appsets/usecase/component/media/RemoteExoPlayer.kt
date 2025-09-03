package xcj.app.appsets.usecase.component.media

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.service.MediaPlayback101Service
import xcj.app.appsets.ui.model.state.SpotLight
import xcj.app.appsets.usecase.MediaRemoteExoUseCase
import xcj.app.starter.android.functions.timestampToMSS
import xcj.app.starter.android.util.PurpleLogger

class RemoteExoPlayer(
    private val audioPlayerState: MutableState<SpotLight.AudioPlayer>
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "RemoteExoPlayer"
    }

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var mediaController: MediaController

    private var mLifecycleOwner: LifecycleOwner? = null

    private var progressRetrieveJob: Job? = null

    fun isPlaying(): Boolean {
        return isBound() && mediaController.isPlaying
    }

    fun isBound(): Boolean {
        return ::controllerFuture.isInitialized
                && controllerFuture.isDone
                && ::mediaController.isInitialized
                && mediaController.isConnected
    }

    @SuppressLint("RestrictedApi")
    private fun initializeController(context: Context, onDone: (() -> Unit)? = null) {
        if (::controllerFuture.isInitialized) {
            return
        }
        PurpleLogger.current.d(TAG, "initializeController")
        val applicationContext = context.applicationContext
        val serviceComponent =
            ComponentName(applicationContext, MediaPlayback101Service::class.java)
        val sessionToken = SessionToken(applicationContext, serviceComponent)
        val mediaControllerBuilder = MediaController.Builder(applicationContext, sessionToken)
        val controllerFuture = mediaControllerBuilder.buildAsync()
        this.controllerFuture = controllerFuture
        val futureCallback = object : FutureCallback<MediaController> {
            override fun onSuccess(result: MediaController) {
                PurpleLogger.current.d(TAG, "initializeController, Futures onSuccess")
                mediaController = result
                setupController(result)
            }

            override fun onFailure(t: Throwable) {
                PurpleLogger.current.d(TAG, "initializeController, Futures onFailure")
            }
        }
        Futures.addCallback(controllerFuture, futureCallback, MoreExecutors.directExecutor())
        controllerFuture.get()
        onDone?.invoke()
    }

    private fun setupController(controller: MediaController) {
        val playerListener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                PurpleLogger.current.d(TAG, "onEvents:$events")
                if (events.containsAny(
                        Player.EVENT_IS_PLAYING_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED
                    )
                ) {
                    updateAudioPlaybackState(player.playbackState)
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                PurpleLogger.current.d(TAG, "onMediaMetadataChanged")
                updateMediaMetadata(mediaMetadata)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                PurpleLogger.current.d(TAG, "onMediaItemTransition")
                val mediaMetadata = mediaItem?.mediaMetadata
                if (mediaMetadata != null) {
                    updateMediaMetadata(mediaMetadata)
                }
            }

            override fun onTracksChanged(tracks: Tracks) {
                PurpleLogger.current.d(TAG, "onTracksChanged")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                PurpleLogger.current.d(
                    TAG,
                    "onPlaybackStateChanged:$playbackState"
                )
                updateAudioPlaybackState(playbackState)
                if (playbackState == Player.STATE_READY && mediaController.playWhenReady) {
                    mLifecycleOwner?.lifecycleScope?.launch {
                        startRetrieveProgress(mediaController)
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    endRetrieveProgress()
                }
            }
        }
        controller.addListener(playerListener)
    }

    private fun endRetrieveProgress() {
        progressRetrieveJob?.cancel()
        PurpleLogger.current.d(TAG, "endRetrieveProgress")
    }

    private suspend fun startRetrieveProgress(mediaController: MediaController) {
        val itemDurationMills = mediaController.duration
        PurpleLogger.current.d(TAG, "startRetrieveProgress, duration in ms:${itemDurationMills}")
        if (itemDurationMills == C.TIME_UNSET) {
            PurpleLogger.current.d(TAG, "startRetrieveProgress, duration <=0, return!")
            return
        }
        progressRetrieveJob?.cancel()
        progressRetrieveJob = coroutineScope {
            launch {
                val oldState = audioPlayerState.value
                val interval = 30L
                val count = itemDurationMills / interval
                for (i in 0..count) {
                    val newState = oldState.copy(
                        duration = timestampToMSS(itemDurationMills),
                        durationRawValue = itemDurationMills,
                        currentDurationRawValue = mediaController.currentPosition,
                        currentDuration = timestampToMSS(mediaController.currentPosition)
                    )
                    audioPlayerState.value = newState
                    PurpleLogger.current.d(TAG, "startRetrieveProgress:newState:${newState}")
                    delay(interval)
                }
            }
        }
    }

    private fun updateMediaMetadata(metadata: MediaMetadata) {
        PurpleLogger.current.d(TAG, "updateMediaMetadata")
        if (!isBound()) {
            return
        }
        val mediaController = this.mediaController
        val oldState = audioPlayerState.value
        val duration = if (mediaController.duration == C.TIME_UNSET) {
            0
        } else {
            mediaController.duration
        }
        audioPlayerState.value = oldState.copy(
            mediaMetadata = metadata,
            duration = timestampToMSS(duration),
            durationRawValue = duration,
            currentDuration = "00:00"
        )
    }

    private fun releaseController() {
        if (!::controllerFuture.isInitialized) {
            return
        }
        MediaController.releaseFuture(controllerFuture)
    }

    private fun updateAudioPlaybackState(state: Int) {
        val oldState = audioPlayerState.value
        audioPlayerState.value = oldState.copy(playbackState = state)
    }

    fun playAudio(context: Context, mediaJson: CommonURIJson) {
        PurpleLogger.current.d(TAG, "playAudio, mediaController:$mediaController")
        if (!isBound()) {
            initializeController(context) {
                playAudio(context, mediaJson)
            }
            return
        }
        val oldState = audioPlayerState.value

        if (oldState.id == mediaJson.id) {
            //same audio
            mediaController.play()
            return
        }
        //new audio to play
        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(mediaJson.name)
            .setExtras(Bundle().apply {
                putString(
                    android.media.MediaMetadata.METADATA_KEY_MEDIA_ID,
                    mediaJson.uri
                )
            })
            .build()
        audioPlayerState.value = oldState.copy(
            id = mediaJson.id,
            mediaMetadata = mediaMetadata
        )
        val commandBundle = Bundle().apply {
            putString("url", mediaJson.uri)
        }
        mediaController.sendCustomCommand(
            SessionCommand("set_playback_item", commandBundle),
            Bundle.EMPTY
        )
    }

    fun playOrPauseAudio(context: Context, musicURLJson: CommonURIJson) {
        val oldState = audioPlayerState.value
        if (oldState.id == musicURLJson.id) {
            if (isPlaying()) {
                pause()
            } else {
                play()
            }
            return
        }
        playAudio(context, musicURLJson)
    }

    fun play() {
        if (!isBound()) {
            return
        }
        mediaController.play()
    }


    fun pause() {
        if (!isBound()) {
            return
        }
        mediaController.pause()
    }

    fun stop() {
        if (!isBound()) {
            return
        }
        mediaController.stop()
    }

    fun seekTo(duration: Long) {
        if (!isBound()) {
            return
        }
        mediaController.seekTo(duration)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        /*owner.asContextOrNull()?.let { context ->
            initializeController(context)
        }*/
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        releaseController()
    }

    fun isPreparingState(): Boolean {
        if (!isBound()) {
            return true
        }
        val audioPlayer = audioPlayerState.value
        if (audioPlayer.id == MediaRemoteExoUseCase.DEFAULT_EMPTY_UUID) {
            return true
        }
        if (audioPlayer.title == Constants.STR_EMPTY) {
            return true
        }
        if (audioPlayer.currentDuration == Constants.STR_EMPTY) {
            return true
        }

        if (isPlaying()) {
            return false
        }
        return false
    }
}