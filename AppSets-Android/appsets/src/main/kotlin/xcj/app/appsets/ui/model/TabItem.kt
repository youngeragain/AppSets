package xcj.app.appsets.ui.model
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.model.state.SpotLight
import java.util.UUID

sealed interface TabAction {
    val action: String?
    val icon: Int
    val isVisible: Boolean
    val actionId: UUID
    val route: String?

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
    val actions: MutableList<TabAction>?
    val tabId: UUID

    val isVisible: Boolean
        @Composable get

    val transFormTextColor: Color
        @Composable get() {
            return if (isSelect) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        }
    val transFormIconTintColor: Color
        @Composable get() {
            return if (isSelect) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        }

    data class SampleTabItem(
        override val routeName: String,
        override val icon: Int,
        override val showDescription: Boolean = false,
        override val name: Int? = null,
        override val description: String? = null,
        override val isSelect: Boolean = false,
        override val actions: MutableList<TabAction>? = null,
        override val tabId: UUID = UUID.randomUUID()
    ) : TabItem {
        override val isVisible: Boolean
            @Composable get() = true
    }

    data class PlaybackTabItem(
        override val routeName: String,
        override val icon: Int,
        override val showDescription: Boolean = false,
        override val name: Int? = null,
        override val description: String? = null,
        override val isSelect: Boolean = false,
        override val actions: MutableList<TabAction>? = null,
        override val tabId: UUID = UUID.randomUUID(),
        val playbackItem: SpotLight.AudioPlayer? = null
    ) : TabItem {

        override val isVisible: Boolean
            @Composable get() {
                return LocalUseCaseOfMediaRemoteExo.current.isPlaying
            }
    }
}