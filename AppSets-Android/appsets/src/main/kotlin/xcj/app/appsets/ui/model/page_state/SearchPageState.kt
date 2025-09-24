package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.R
import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.appsets.ui.model.state.SearchResult

sealed interface SearchPageState : TipsProvider {
    val keywords: String?

    class None(
        override val keywords: String? = null,
        override val tipsIntRes: Int? = null,
        override val subTipsIntRes: Int? = null
    ) : SearchPageState

    class Searching(
        override val keywords: String? = null,
        override val tipsIntRes: Int = R.string.searching,
        override val subTipsIntRes: Int? = null
    ) : SearchPageState

    class SearchPageSuccess(
        override val keywords: String? = null,
        override val tipsIntRes: Int? = null,
        override val subTipsIntRes: Int? = null,
        val results: List<SearchResult>,
    ) : SearchPageState

    class SearchPageFailed(
        override val keywords: String? = null,
        override val tipsIntRes: Int? = null,
        override val subTipsIntRes: Int? = null
    ) : SearchPageState

}