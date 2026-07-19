package xcj.app.appsets.ui.compose.content_selection

import xcj.app.appsets.ui.compose.content_selection.ContentSelectionRequest.SelectionTypeParam
import xcj.app.appsets.util.compose_state.ComposeStateUpdater

typealias CountProvider = (String) -> Int

private val defaultMaxCountProvider: CountProvider
    get() = { 1 }

data class ContentSelectionRequest(
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

    private fun buildMarkKey(): String {
        return "${contextName}/${requestKey}/${defaultSelectionType}/${
            selectionTypeMaxCount(
                defaultSelectionType
            )
        }}"
    }

    suspend fun handleResult(contentSelectionResult: ContentSelectionResult<*>) {
        val markKey = buildMarkKey()
        composeStateUpdater?.input(markKey, contentSelectionResult)
    }
}

fun defaultImageSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.IMAGE, countProvider)
    )

fun defaultVideoSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.VIDEO, countProvider)
    )

fun defaultAudioSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.AUDIO, countProvider)
    )

fun defaultFileSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.FILE, countProvider)
    )

fun defaultLocationSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.LOCATION, countProvider)
    )

fun defaultCameraSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.CAMERA, countProvider)
    )

fun defaultMediaSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.IMAGE, countProvider),
        SelectionTypeParam(ContentSelectionTypes.VIDEO, countProvider),
        SelectionTypeParam(ContentSelectionTypes.AUDIO, countProvider),
    )

fun defaultAllSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    defaultImageSelectionTypeParam(countProvider) +
            defaultVideoSelectionTypeParam(countProvider) +
            defaultAudioSelectionTypeParam(countProvider) +
            defaultFileSelectionTypeParam(countProvider) +
            defaultLocationSelectionTypeParam(countProvider) +
            defaultCameraSelectionTypeParam(countProvider)