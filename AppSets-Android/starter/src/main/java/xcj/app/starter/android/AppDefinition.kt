package xcj.app.starter.android

import android.content.pm.ApplicationInfo
import android.net.Uri

data class AppDefinition(
    val id: String
) : ItemDefinition, Pinable {
    var applicationInfo: ApplicationInfo? = null
    override var icon: Any? = null
    override var name: String? = null
    override var uri: Uri? = null
    override var description: String? = null
    override var isPinned: Boolean = false

    companion object {
        private const val TAG = "AppDefinition"
    }
}
