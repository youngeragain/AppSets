package xcj.app.appsets.im.message

import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResults

class LocationMessageMetadata(
    description: String,
    size: Int,
    compressed: Boolean,
    encode: String,
    contentType: String,
    data: ContentSelectionResults.LocationInfo,
) : MessageMetadata<ContentSelectionResults.LocationInfo>(
    description, size, compressed, encode, data, contentType
)