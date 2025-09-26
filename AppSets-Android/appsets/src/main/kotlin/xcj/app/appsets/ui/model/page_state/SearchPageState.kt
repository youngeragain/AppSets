package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.appsets.ui.model.state.SearchResult

sealed interface SearchPageState : TipsProvider {
    val keywords: String?

    class None(
        override val keywords: String? = null,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SearchPageState

    class Searching(
        override val keywords: String? = null,
        override val tips: Int = xcj.app.appsets.R.string.searching,
        override val subTips: Int? = null
    ) : SearchPageState

    class SearchPageSuccess(
        override val keywords: String? = null,
        override val tips: Int? = null,
        override val subTips: Int? = null,
        val results: List<SearchResult>,
    ) : SearchPageState

    class SearchPageFailed(
        override val keywords: String? = null,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : SearchPageState

}