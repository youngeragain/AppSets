package xcj.app.appsets.ui.compose.media.video.single

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.google.gson.Gson
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.usecase.MediaLocalExoUseCase
import xcj.app.starter.android.ui.base.DesignViewModel
import xcj.app.starter.android.util.PurpleLogger

class MediaPlaybackViewModel : DesignViewModel() {

    companion object {
        private const val TAG = "ExoPlayerViewModel"
    }

    private var videoUriJson: CommonURIJson? = null

    val mediaLocalExoUseCase: MediaLocalExoUseCase = MediaLocalExoUseCase()

    init {
        PurpleLogger.current.d(TAG, "init, hash:${hashCode()}")
    }

    fun initVideoJson(videoUriJson: String?) {
        if (videoUriJson.isNullOrEmpty()) {
            return
        }
        runCatching {
            this.videoUriJson =
                Gson().fromJson(videoUriJson, CommonURIJson::class.java)
        }
    }

    fun attachPlayerView(playerView: PlayerView) {
        mediaLocalExoUseCase.attachPlayerView(playerView)
    }

    fun detachPlayerView(playerView: PlayerView?) {
        mediaLocalExoUseCase.detachPlayerView(playerView)
    }

    fun play() {
        var videoURIJson = videoUriJson ?: return
        mediaLocalExoUseCase.playVideo(videoURIJson, true)
    }

    fun onActivityCreated(activity: ComponentActivity) {
        mediaLocalExoUseCase.setLifecycleOwner(activity)
    }

    @OptIn(UnstableApi::class)
    override fun handleIntent(intent: Intent) {
        initVideoJson(intent.getStringExtra(MediaPlaybackActivity.KEY_VIDEO_JSON_DATA))
    }
}