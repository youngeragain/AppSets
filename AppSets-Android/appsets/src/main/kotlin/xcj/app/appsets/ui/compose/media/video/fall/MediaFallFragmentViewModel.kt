package xcj.app.appsets.ui.compose.media.video.fall

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import xcj.app.appsets.ui.base.InfinityPagerAdapter
import xcj.app.starter.android.ui.base.DesignViewModel
import xcj.app.starter.android.util.PurpleLogger

class MediaFallFragmentViewModel : DesignViewModel() {
    companion object {
        private const val TAG = "MediaFallFragmentViewModel"
    }

    private var mRealPosition: Int = 0
    private var mLogicPosition: Int = 0

    val positionInfo: String
        get() =
            "position[real,logic]:[$mRealPosition, $mLogicPosition]"

    val videoMediaContent: MutableState<VideoMediaContent?> = mutableStateOf(null)

    val lifecycleState: MutableState<Lifecycle.State> = mutableStateOf(Lifecycle.State.INITIALIZED)

    init {
        PurpleLogger.current.d(TAG, "init")
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun setCurrentVideoMediaContent(videoMediaContent: VideoMediaContent) {
        this.videoMediaContent.value = videoMediaContent
    }

    fun getCurrentVideoMediaContent(): VideoMediaContent? {
        return videoMediaContent.value
    }

    fun onAttach(fragment: MediaFallFragment) {
        fragment.arguments?.run {
            mRealPosition = getInt(InfinityPagerAdapter.Companion.REAL_POSITION)
            mLogicPosition = getInt(InfinityPagerAdapter.Companion.LOGIC_POSITION)
        }
    }
}