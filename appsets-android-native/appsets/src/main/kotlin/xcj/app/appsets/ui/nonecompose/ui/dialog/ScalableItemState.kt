package xcj.app.appsets.ui.nonecompose.ui.dialog

import android.net.Uri
import xcj.app.appsets.ui.nonecompose.base.Diffable
import xcj.app.appsets.ui.nonecompose.base.UriHolder

data class ScalableItemState(
    val id: Int? = null,
    var any: Any? = null,
    var selected: Boolean = false,
    var showDelete: Boolean = false,
    override var type: Int = ScalableItemAdapter.TYPE_PIC_SELECTABLE
) : Diffable, UriHolder {
    override fun provideUri(): Uri? {
        return any as? Uri
    }
}