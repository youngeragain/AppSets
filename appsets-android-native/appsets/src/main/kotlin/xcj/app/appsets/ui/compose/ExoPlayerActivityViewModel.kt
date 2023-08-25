package xcj.app.appsets.ui.compose

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.google.gson.Gson
import xcj.app.appsets.im.CommonURLJson
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.usecase.MediaUseCase
import java.util.UUID

@UnstableApi
class ExoPlayerActivityViewModel : BaseViewModel() {
    val mediaUseCase: MediaUseCase = MediaUseCase(arrayOf("local"), true)
    private var videoJson: CommonURLJson.VideoURLJson? = null

    init {
        Log.e("ExoPlayerVM", "init, hash:${hashCode()}")
    }

    fun initVideoJson(videoJson: String?) {
        if (videoJson.isNullOrEmpty())
            return
        val videoJson1 =
            Gson().fromJson(videoJson, CommonURLJson.VideoURLJson::class.java)
        this.videoJson = videoJson1
    }

    fun playWith(playerView: PlayerView) {
        mediaUseCase.playWith(playerView)
    }
    var palyId: String = ""

    fun play() {
        if (videoJson == null)
            return
        palyId = UUID.randomUUID().toString()
        mediaUseCase.playVideo(palyId, videoJson!!)
    }

    val userCustomUiModel: MutableState<Boolean?> = mutableStateOf(true)
    fun userCustomUiMode() {
        userCustomUiModel.value = !(userCustomUiModel.value ?: false)
    }
}