package xcj.app.appsets.ui.compose.win11Snapshot

import android.net.Uri

data class RecommendedItemDefinition(
    override val icon: Any?,
    override val name: String,
    override val description: String?
) : ItemDefinition {
    override var uri: Uri? = null
}