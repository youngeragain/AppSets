package xcj.app.appsets.ui.compose.content_selection

import android.content.Context
import xcj.app.appsets.util.compose_state.ComposeStateUpdater

data class ContentSelectionRequest(
    val context: Context,
    val contextName: String,
    val requestKey: String,
    val selectionTypeParams: List<SelectionTypeParam>,
    val defaultSelectionType: String,
    val composeStateUpdater: ComposeStateUpdater<*>? = null
) {
    data class SelectionTypeParam(
        val selectionType: String,
        val maxCount: CountProvider
    )

    fun selectionTypeMaxCount(selectionType: String): Int {
        return selectionTypeParams.firstOrNull {
            it.selectionType == selectionType
        }?.maxCount(selectionType) ?: 0
    }

    fun buildMarkKey(): String {
        return "${contextName}/${requestKey}/${defaultSelectionType}/${
            selectionTypeMaxCount(
                defaultSelectionType
            )
        }}"
    }
}