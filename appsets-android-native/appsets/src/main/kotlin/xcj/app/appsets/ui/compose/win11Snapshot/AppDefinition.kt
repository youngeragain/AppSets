package xcj.app.appsets.ui.compose.win11Snapshot

import android.net.Uri


data class AppDefinition(
    override val icon: Any?,
    val packageName: String = "",
    override val name: String,
    val launcherActivityClass:String?=null
) : ItemDefinition, Pinned {
    override var uri: Uri? = null
    override var description: String? = null
    override var isPinned: Boolean = false
}
