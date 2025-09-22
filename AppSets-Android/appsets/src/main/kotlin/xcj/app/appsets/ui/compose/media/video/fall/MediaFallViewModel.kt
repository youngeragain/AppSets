package xcj.app.appsets.ui.compose.media.video.fall

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.viewModelScope
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.server.model.MediaContent
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.usecase.MediaLocalExoUseCase
import xcj.app.starter.android.ui.base.DesignViewModel
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.request
import java.util.UUID

data class VideoMediaContent(
    val mediaContent: MediaContent,
    var id: String = UUID.randomUUID().toString(),
    var isViewed: Boolean = false,
)

class MediaFallViewModel : DesignViewModel() {
    companion object {
        private const val TAG = "MediaFallViewModel"
    }

    private var page: Int = 1
    private var pageSize: Int = 10
    private var lastContentSize = 0

    private val mediaLocalExoUseCase: MediaLocalExoUseCase = MediaLocalExoUseCase()

    private val appSetsRepository: AppSetsRepository = AppSetsRepository.getInstance()

    val currentPagerPosition: MutableState<Int> = mutableIntStateOf(0)

    val currentServerVideoMediaContentList: MutableList<VideoMediaContent> = mutableListOf()


    private fun loadData(init: Boolean = false) {
        if (init) {
            page = 1
            lastContentSize = 0
            requestData()
            return
        }
        if (lastContentSize == pageSize) {
            page += 1
            requestData()
        }
    }

    private fun requestData() {
        viewModelScope.launch {
            request {
                appSetsRepository.getMediaContent("video", page, pageSize)
            }.onSuccess {
                PurpleLogger.current.d(TAG, "requestData: success, media content:${it}")
                if (page == 1) {
                    if (currentServerVideoMediaContentList.isNotEmpty()) {
                        currentServerVideoMediaContentList.clear()
                    }
                    currentServerVideoMediaContentList.addAll(it.map { mediaContent ->
                        VideoMediaContent(
                            mediaContent
                        )
                    })
                } else if (page > 1) {
                    currentServerVideoMediaContentList.addAll(it.map { mediaContent ->
                        VideoMediaContent(
                            mediaContent
                        )
                    })
                }
                lastContentSize = it.size
            }.onFailure {
                PurpleLogger.current.d(TAG, "requestData failed!, e:$it")
            }
        }
    }

    private fun prepareForFragment(mediaFallFragment: MediaFallFragment) {
        PurpleLogger.current.d(TAG, "prepareForFragment")
        tryGetContentForFragment(mediaFallFragment)
        loadMoreDataIfNeeded()
    }

    private fun tryGetContentForFragment(mediaFallFragment: MediaFallFragment) {
        PurpleLogger.current.d(TAG, "tryGetContentForFragment")
        viewModelScope.launch {
            val existVideoMediaContent = mediaFallFragment.getLoadedVideoMediaContent()
            if (existVideoMediaContent != null) {
                PurpleLogger.current.d(TAG, "tryGetContentForFragment, use exist")
                prepareUriVideo(existVideoMediaContent, true)
                return@launch
            }
            var tryTimes = 0
            while (tryTimes < 10) {
                if (currentServerVideoMediaContentList.isEmpty()) {
                    tryTimes++
                    delay(1000)
                } else {
                    break
                }
            }
            if (currentServerVideoMediaContentList.isEmpty()) {
                return@launch
            }
            val videoMediaContent = currentServerVideoMediaContentList.random()
            videoMediaContent.isViewed = true
            videoMediaContent.id = UUID.randomUUID().toString()
            mediaFallFragment.setLoadedVideoMediaContent(videoMediaContent)
            PurpleLogger.current.d(TAG, "tryGetContentForFragment, use new")
            prepareUriVideo(videoMediaContent, true)
        }
    }

    private fun loadMoreDataIfNeeded() {
        var allVideoViewed = true
        for (videoMediaContent in currentServerVideoMediaContentList) {
            if (!videoMediaContent.isViewed) {
                allVideoViewed = false
                break
            }
        }
        if (allVideoViewed && currentServerVideoMediaContentList.isNotEmpty()) {
            loadData(false)
        }
    }

    private fun prepareUriVideo(videoMediaContent: VideoMediaContent, playWhenReady: Boolean) {
        PurpleLogger.current.d(TAG, "prepareUriVideo")
        mediaLocalExoUseCase.prepareUriVideo(videoMediaContent, playWhenReady)
    }

    fun attachPlayerView(playerView: PlayerView) {
        mediaLocalExoUseCase.attachPlayerView(playerView)
    }

    fun detachPlayerView(playerView: PlayerView?) {
        mediaLocalExoUseCase.detachPlayerView(playerView)
    }

    fun pause(context: Context) {
        mediaLocalExoUseCase.pause()
    }

    fun onActivityCreated(activity: ComponentActivity) {
        mediaLocalExoUseCase.setLifecycleOwner(activity)
        loadData(true)
    }

    fun getCurrentPosition(): Int {
        return currentPagerPosition.value
    }

    fun updateCurrentPagerPosition(mediaFallFragment: MediaFallFragment?, position: Int) {
        PurpleLogger.current.d(TAG, "updateCurrentPagerPosition, position:$position")
        currentPagerPosition.value = position
        if (mediaFallFragment != null) {
            prepareForFragment(mediaFallFragment)
        }
    }
}