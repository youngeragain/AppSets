package xcj.app.appsets.im.message

import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult

class LocationMessageMetadata(
    description: String,
    size: Int,
    compressed: Boolean,
    encode: String,
    contentType: String,
    data: ContentSelectionResult.LocationContentSelectionResult.LocationInfo,
) : MessageMetadata<ContentSelectionResult.LocationContentSelectionResult.LocationInfo>(
    description, size, compressed, encode, data, contentType
)