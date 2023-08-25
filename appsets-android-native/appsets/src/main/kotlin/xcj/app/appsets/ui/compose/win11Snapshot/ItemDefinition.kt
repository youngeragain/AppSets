package xcj.app.appsets.ui.compose.win11Snapshot

import android.net.Uri

interface ItemDefinition {
    val icon: Any?
    val name: String
    val description: String?
    var uri: Uri?
}