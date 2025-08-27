package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.server.model.MediaContent
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.SpotLightState
import xcj.app.appsets.usecase.component.media.RemoteExoPlayer
import xcj.app.compose_share.dynamic.IComposeLifecycleAware

class MediaRemoteExoUseCase(
    private val coroutineScope: CoroutineScope,
    private val appSetsRepository: AppSetsRepository,
) : IComposeLifecycleAware {

    companion object {
        private const val TAG = "MediaRemoteExoUseCase"
        const val DEFAULT_EMPTY_UUID = "00000000-0000-0000-0000-000000000000"
    }

    val audioPlayerState: MutableState<SpotLightState.AudioPlayer> =
        mutableStateOf(SpotLightState.AudioPlayer())

    val isPlaying: Boolean
        get() = remoteExoPlayer.isPlaying()

    private val remoteExoPlayer: RemoteExoPlayer = RemoteExoPlayer(coroutineScope, audioPlayerState)

    val serverMusicMediaContentList: MutableState<List<MediaContent>?> = mutableStateOf(null)

    var currentPlaybackMediaContent: MediaContent? = null

    fun pauseAudio() {
        remoteExoPlayer.pause()
    }

    fun playAudio(context: Context, musicURLJson: CommonURIJson) {
        remoteExoPlayer.playAudio(context, musicURLJson)
    }

    fun playOrPauseAudio(context: Context, musicURLJson: CommonURIJson) {
        remoteExoPlayer.playOrPauseAudio(context, musicURLJson)
    }

    fun requestPause() {
        remoteExoPlayer.pause()
    }

    fun requestPlay() {
        remoteExoPlayer.play()
    }

    fun requestSeekTo(duration: Long) {
        remoteExoPlayer.seekTo(duration)
    }

    fun isPreparingState(): Boolean {
        return remoteExoPlayer.isPreparingState()
    }

    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(remoteExoPlayer)
    }

    override fun onComposeDispose(by: String?) {

    }
}

