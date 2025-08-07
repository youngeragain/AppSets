package xcj.app.appsets.ui.compose.content_selection

import android.net.Uri
import xcj.app.appsets.util.model.MediaStoreDataUri

/**
 * 选择内容弹窗
 */
object ContentSelectionTypes {

    const val CAMERA = "camera"
    const val LOCATION = "location"

    const val IMAGE = "image"

    const val VIDEO = "video"
    const val AUDIO = "audio"
    const val FILE = "file"

    const val SELECTED_URI_LIST = "selected_uri_list"

    private fun removeDuplicateAllByPlatform(
        rawList: MutableList<MediaStoreDataUri>,
        items: List<Uri>?
    ) {
        items ?: return
        rawList.removeIf { wrapper ->
            items.firstOrNull { it.path == wrapper.uri?.path } != null
        }
    }

}