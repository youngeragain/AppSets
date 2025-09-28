package xcj.app.appsets.ui.compose.content_selection

import android.content.Context
import xcj.app.appsets.util.model.UriProvider

sealed interface ContentSelectionResult {
    val context: Context
    val request: ContentSelectionRequest
    val selectType: String

    data class RichMediaContentSelectionResult(
        override val context: Context,
        override val request: ContentSelectionRequest,
        override val selectType: String,
        val selectItems: List<UriProvider>,
    ) : ContentSelectionResult

    data class LocationContentSelectionResult(
        override val context: Context,
        override val request: ContentSelectionRequest,
        override val selectType: String,
        val locationInfo: LocationInfo,
    ) : ContentSelectionResult {
        data class LocationInfo(
            val coordinate: String,
            val info: String? = null,
            val extras: String? = null,
        )
    }
}
