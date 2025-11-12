package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.appsets.ui.model.state.SearchResult

sealed interface SearchPageUIState : TipsProvider {
    val keywords: String?

    class SearchStart(
        override val keywords: String? = null,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SearchPageUIState

    class Searching(
        override val keywords: String? = null,
        override val tips: Int = xcj.app.appsets.R.string.searching,
        override val subTips: Int? = null
    ) : SearchPageUIState

    class SearchSuccess(
        override val keywords: String? = null,
        override val tips: Int? = null,
        override val subTips: Int? = null,
        val results: List<SearchResult>,
    ) : SearchPageUIState

    class SearchFailed(
        override val keywords: String? = null,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SearchPageUIState

}