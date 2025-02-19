package xcj.app.appsets.ui.model

import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.UserInfo

sealed interface SearchResult {
    data class SearchedUser(val userInfo: UserInfo) : SearchResult

    data class SearchedGroup(val groupInfo: GroupInfo) :
        SearchResult

    data class SearchedScreen(val screenInfo: ScreenInfo) :
        SearchResult

    data class SearchedApplications(
        val applications: List<Application>
    ) : SearchResult

    data class SplitTitle(val title: Int? = null) : SearchResult
}

sealed class SearchState(val keywords: String? = null, override val tips: Int? = null) : TipsState {

    class None(tips: Int? = null) : SearchState(null, tips)

    class Searching(keywords: String) : SearchState(keywords, xcj.app.appsets.R.string.searching)

    class SearchSuccess(
        keywords: String,
        val results: List<SearchResult>,
        override val tips: Int? = null
    ) :
        SearchState(keywords, tips)

    class SearchFailed(keywords: String, tips: Int? = null) : SearchState(keywords, tips)

}