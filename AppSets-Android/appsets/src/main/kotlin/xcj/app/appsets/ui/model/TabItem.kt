package xcj.app.appsets.ui.model

import androidx.compose.runtime.Composable
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.model.state.SpotLight
import java.util.UUID

sealed interface TabAction {
    val action: String?
    val icon: Int
    val actionId: UUID
    val route: String?
    val isVisible: Boolean

    @Composable
    fun isShow(): Boolean {
        return isVisible
    }

    data class SampleTabAction(
        override val action: String? = null,
        override val icon: Int,
        override val isVisible: Boolean = true,
        override val actionId: UUID = UUID.randomUUID(),
        override val route: String? = null,
        val name: String? = null,
        val description: String? = null,
    ) : TabAction

    companion object {
        const val ACTION_REFRESH = "ACTION_REFRESH"
        const val ACTION_ADD = "ACTION_ADD"
        const val ACTION_APP_TOOLS = "ACTION_APP_TOOLS"
    }
}

sealed interface TabItem {
    val routeName: String
    val icon: Int
    val showDescription: Boolean
    val name: Int?
    val description: String?
    val isSelect: Boolean
    val tabId: UUID
    val actions: MutableList<TabAction>?
    var isVisible: Boolean

    @Composable
    fun isShow(): Boolean {
        return isVisible
    }

    data class SampleTabItem(
        override val routeName: String,
        override val icon: Int,
        override val showDescription: Boolean = false,
        override val name: Int? = null,
        override val description: String? = null,
        override val isSelect: Boolean = false,
        override val actions: MutableList<TabAction>? = null,
        override val tabId: UUID = UUID.randomUUID(),
        override var isVisible: Boolean = true
    ) : TabItem

    data class PlaybackTabItem(
        override val routeName: String,
        override val icon: Int,
        override val showDescription: Boolean = false,
        override val name: Int? = null,
        override val description: String? = null,
        override val isSelect: Boolean = false,
        override val actions: MutableList<TabAction>? = null,
        override val tabId: UUID = UUID.randomUUID(),
        override var isVisible: Boolean = true,
        val playbackItem: SpotLight.AudioPlayer? = null,
    ) : TabItem {
        @Composable
        override fun isShow(): Boolean {
            return isVisible &&
                    LocalUseCaseOfMediaRemoteExo.current.isPlaying
        }
    }
}