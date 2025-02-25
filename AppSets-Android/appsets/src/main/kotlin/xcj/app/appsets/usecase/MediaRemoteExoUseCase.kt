package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.server.model.MediaContent
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.SpotLightState
import xcj.app.appsets.usecase.component.media.RemoteExoplayer
import xcj.app.compose_share.dynamic.IComposeDispose

class MediaRemoteExoUseCase(
    private val coroutineScope: CoroutineScope,
    private val appSetsRepository: AppSetsRepository,
) : IComposeDispose {

    companion object {
        private const val TAG = "MediaRemoteExoUseCase"
        const val DEFAULT_EMPTY_UUID = "00000000-0000-0000-0000-000000000000"
    }

    val audioPlayerState: MutableState<SpotLightState.AudioPlayer> =
        mutableStateOf(SpotLightState.AudioPlayer())

    val isPlaying: Boolean
        get() = remoteExoplayer.isPlaying

    private val remoteExoplayer: RemoteExoplayer = RemoteExoplayer(coroutineScope, audioPlayerState)

    val serverMusicMediaContentList: MutableState<List<MediaContent>?> = mutableStateOf(null)

    var currentPlaybackMediaContent: MediaContent? = null

    fun pauseAudio() {
        remoteExoplayer.pause()
    }

    fun playAudio(musicURLJson: CommonURIJson) {
        remoteExoplayer.playAudio(musicURLJson)
    }

    fun playOrPauseAudio(musicURLJson: CommonURIJson) {
        remoteExoplayer.playOrPauseAudio(musicURLJson)
    }

    fun requestPause() {
        remoteExoplayer.pause()
    }

    fun requestPlay() {
        remoteExoplayer.play()
    }

    fun requestSeekTo(duration: Long) {
        remoteExoplayer.seekTo(duration)
    }

    fun isPreparingState(): Boolean {
        return remoteExoplayer.isPreparingState()
    }


    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(remoteExoplayer)
    }

    override fun onComposeDispose(by: String?) {

    }
}

