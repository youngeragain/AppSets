package xcj.app.appsets.ui.model

import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult

data class SelectedContents(
    val contents: MutableMap<String, ContentSelectionResult> = mutableMapOf()
)