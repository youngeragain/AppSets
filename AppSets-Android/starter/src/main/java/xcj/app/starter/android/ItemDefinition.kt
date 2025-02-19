package xcj.app.starter.android

import android.net.Uri

interface ItemDefinition {
    val icon: Any?
    val name: String?
    val description: String?
    var uri: Uri?
}