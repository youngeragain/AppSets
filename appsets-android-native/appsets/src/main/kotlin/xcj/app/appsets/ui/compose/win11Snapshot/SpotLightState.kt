package xcj.app.appsets.ui.compose.win11Snapshot

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.compose.ui.graphics.Color
import xcj.app.appsets.server.model.Hotsearch

sealed class SpotLightState {
    interface ClickInterface {
        val onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)?

        companion object Template : ClickInterface {
            override var onClick: ((SpotLightState, Context, Any?) -> Unit)? =
                { state, context, payload ->
                    Log.e(
                        "ClickInterface",
                        "Template onClick: state:$state context:$context, payload:$payload"
                    )
                }
        }
    }

    data class TodayInHistory(
        val bgColor: Color?,
        var bgImg: Any?,
        var title: String?,
        val content: String
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class WordOfTheDay(
        val bgColor: Color?,
        val bgImg: Any?,
        val content: String,
        val author: String
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class PinnedApps(
        var apps: List<AppDefinition>
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class RecommendedItems(
        var items: List<ItemDefinition>
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class HeaderTitle(
        val time: String
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class QuestionOfTheDay(
        val img: Any?,
        val where: String,
        val whereBelowText: String,
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class Holiday(
        val img: Any?,
        val name: String,
        val info_url: String?,
        val more_url: String?
    ) : SpotLightState(), ClickInterface {
        override var onClick: ((state: SpotLightState, context: Context, payload: Any?) -> Unit)? =
            ClickInterface.onClick
    }

    data class WordOfTheDayAndTodayInHistory(
        val wordOfTheDay: WordOfTheDay?,
        val todayInHistory: TodayInHistory?
    ) : SpotLightState()

    data class PopularSearches(
        val icon: Int,
        val title: String,
        val words: List<String>
    ) : SpotLightState()

    data class HotWordsWrapper(
        val from: String,
        val words: List<Hotsearch>
    ) : SpotLightState()

    class AudioPlayer : SpotLightState() {
        var playbackStateCompat: PlaybackStateCompat? = null
        var mediaMetadataCompat: MediaMetadataCompat? = null
        var id: String = ""
        var title: String = ""
        var art: String = ""
        var duration: String = ""
        var currentDuration: String = ""
        val defaultOrder: Int = 1
    }

    data class VideoPlayer(
        var playbackStateCompat: PlaybackStateCompat?,
        var mediaMetadataCompat: MediaMetadataCompat?
    ) : SpotLightState()

    /**
     * 展示一张图片
     */
    data class GalleryPlayer(
        val name: String?,
        val uri: Uri
    ) : SpotLightState()

    object Bar : SpotLightState()
}