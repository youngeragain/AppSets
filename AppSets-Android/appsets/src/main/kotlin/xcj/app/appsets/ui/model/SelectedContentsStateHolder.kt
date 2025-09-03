package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult

class SelectedContentsStateHolder {

    val selectedContentsState: MutableState<SelectedContents> = mutableStateOf(SelectedContents())

    fun updateSelectedContent(contentSelectionResult: ContentSelectionResult) {
        val selectedContents = selectedContentsState.value
        selectedContents.contents.put(
            contentSelectionResult.request.requestKey,
            contentSelectionResult
        )
        selectedContentsState.value = SelectedContents(selectedContents.contents)
    }
}