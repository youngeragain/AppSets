package xcj.app.appsets.ui.compose.content_selection

import android.content.Context
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult.LocationContentSelectionResult.LocationInfo
import xcj.app.starter.android.util.UriProvider
import xcj.app.starter.foundation.Provider

sealed interface ContentSelectionResult<T> {
    val context: Context
    val request: ContentSelectionRequest
    val selectType: String

    val selectedProvider: Provider<T>

    data class RichMediaContentSelectionResult(
        override val context: Context,
        override val request: ContentSelectionRequest,
        override val selectType: String,
        override val selectedProvider: Provider<List<UriProvider>>
    ) : ContentSelectionResult<List<UriProvider>>

    data class LocationContentSelectionResult(
        override val context: Context,
        override val request: ContentSelectionRequest,
        override val selectType: String,
        override val selectedProvider: Provider<LocationInfo>
    ) : ContentSelectionResult<LocationInfo> {

        data class LocationInfo(
            val coordinate: String,
            val info: String? = null,
            val extras: String? = null,
        )
    }
}
