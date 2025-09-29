package xcj.app.appsets.ui.compose.content_selection

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import xcj.app.appsets.util.ktx.getSystemFileUris
import xcj.app.appsets.util.model.UriProvider
import xcj.app.starter.android.util.PurpleLogger

class ContentSelectionResultsProvider(
    private val mediaStoreType: Class<*>? = null
) {
    companion object {
        private const val TAG = "ContentSelectionResultsProvider"
    }

    var page: Int = 1

    /**
     * 已经选择过的图片
     */
    var selectedContentUris: List<Uri> = mutableListOf()


    /**
     * 最大可选数
     */
    var maxSelectCount: Int = Byte.MAX_VALUE.toInt()

    /**
     * 分页加载图片时的中间对象
     */
    val contentUris: MutableList<UriProvider> = mutableStateListOf()

    private var loading: Boolean = false

    private var pageSize: Int = 32

    suspend fun load(context: Context, first: Boolean = false) {
        if (mediaStoreType == null) {
            return
        }
        if (loading) {
            return
        }
        loading = true
        if (first) {
            page = 1
        } else {
            if (contentUris.size < pageSize) {
                return
            } else {
                page++
            }
        }
        getUris(context, page, pageSize)
    }

    private suspend fun getUris(context: Context, page: Int, pageSize: Int) {
        val fromIndex = ((page - 1) * pageSize)
        //checkRange()
        PurpleLogger.current.d(
            TAG,
            "getUris, step1, ${System.currentTimeMillis()}"
        )
        val systemFileUris =
            context.getSystemFileUris(mediaStoreType, 1, fromIndex, pageSize)
        if (systemFileUris.isNullOrEmpty()) {
            PurpleLogger.current.d(TAG, "empty files")
            return
        }
        PurpleLogger.current.d(
            TAG,
            "getUris, step2, ${System.currentTimeMillis()}"
        )
        val selectedFileUrisMap = selectedContentUris.associateBy { it.path }
        val pagedSystemPhotosFileUris = systemFileUris.filterNot {
            selectedFileUrisMap.containsKey(it.uri.path)
        }
        PurpleLogger.current.d(
            TAG,
            "getUris, step3, ${System.currentTimeMillis()}"
        )
        contentUris.addAll(pagedSystemPhotosFileUris)
        loading = false
    }
}