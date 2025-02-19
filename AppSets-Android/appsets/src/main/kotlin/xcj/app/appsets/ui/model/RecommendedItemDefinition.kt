package xcj.app.appsets.ui.model

import android.net.Uri
import xcj.app.starter.android.ItemDefinition

data class RecommendedItemDefinition(
    override val icon: Any?,
    override val name: String?,
    override val description: String?
) : ItemDefinition {

    override var uri: Uri? = null

}