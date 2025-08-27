package xcj.app.appsets.ui.model.state

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.server.model.MicrosoftBingWallpaper
import xcj.app.appsets.usecase.MediaRemoteExoUseCase
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.ItemDefinition

sealed class SpotLight {

    data class PinnedApp(
        val items: List<AppDefinition>
    ) : SpotLight()

    data class RecommendedItem(
        val items: List<ItemDefinition>
    ) : SpotLight()

    data class BingWallpaper(
        val items: List<MicrosoftBingWallpaper>
    ) : SpotLight()

    data class WordOfTheDay(
        val items: List<Any>
    ) : SpotLight()

    data class PopularSearch(
        val icon: Int,
        val title: String,
        val items: List<Any>
    ) : SpotLight()

    data class AudioPlayer(
        val playbackState: Int = Player.STATE_IDLE,
        val mediaMetadata: MediaMetadata? = null,
        val id: String? = MediaRemoteExoUseCase.Companion.DEFAULT_EMPTY_UUID,
        val duration: String = Constants.STR_EMPTY,
        val durationRawValue: Long = 0,
        val currentDuration: String = Constants.STR_EMPTY,
        val currentDurationRawValue: Long = 0,
        val defaultOrder: Int = 1
    ) : SpotLight() {

        val progress: Float
            get() {
                return if (durationRawValue == 0L) {
                    0f
                } else {
                    currentDurationRawValue.toFloat() / durationRawValue.toFloat()
                }
            }

        val title: String
            get() {
                val metadata = mediaMetadata
                if (metadata?.title.isNullOrEmpty() || metadata.title == Constants.STR_NULL_LOWERCASE) {
                    return Constants.STR_EMPTY
                }
                return metadata.title?.toString() ?: Constants.STR_EMPTY
            }
        val art: String
            get() {
                val metadata = mediaMetadata
                if (metadata?.artist.isNullOrEmpty() || metadata.artist == Constants.STR_NULL_LOWERCASE) {
                    return Constants.STR_EMPTY
                }
                return metadata.artist?.toString() ?: Constants.STR_EMPTY
            }
    }

    data class VideoPlayer(
        val playId: String,
        val playbackState: Int? = null,
        val mediaMetadata: MediaMetadata? = null
    ) : SpotLight()

}