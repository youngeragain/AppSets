@file:OptIn(FlowPreview::class)

package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.CombineSearchRes
import xcj.app.appsets.server.repository.SearchRepository
import xcj.app.appsets.ui.model.page_state.SearchPageState
import xcj.app.appsets.ui.model.state.SearchResult
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.server.requestNotNull
import kotlin.time.Duration.Companion.milliseconds

class SearchUseCase(
    private val searchRepository: SearchRepository,
) : IComposeLifecycleAware {

    companion object {
        private const val TAG = "SearchUseCase"
    }

    //保存上一次搜索内容
    val searchPageState: MutableState<SearchPageState> = mutableStateOf(SearchPageState.None())

    private val searchInputFlow: MutableStateFlow<String> = MutableStateFlow("")

    private val searchFlow: Flow<String> = searchInputFlow
        .debounce(200.milliseconds)
        .distinctUntilChanged()
        .onEach(::search)

    private var searchJob: Job? = null

    fun updateKeywords(keywords: String) {
        searchInputFlow.value = keywords
    }

    fun attachToSearchFlow(coroutineScope: CoroutineScope) {
        searchJob = searchFlow.launchIn(coroutineScope)
    }

    fun detachToSearchFlow() {
        searchJob?.cancel()
    }

    private suspend fun search(keywords: String) {
        if (!LocalAccountManager.isLogged()) {
            searchPageState.value =
                SearchPageState.SearchPageFailed(keywords, R.string.login_to_search)
            return
        }

        if (keywords.isEmpty()) {
            searchPageState.value =
                SearchPageState.SearchPageSuccess(
                    keywords,
                    R.string.no_content,
                    emptyList(),
                )
            return
        }

        val searchState = searchPageState.value

        if (searchState is SearchPageState.SearchPageSuccess && searchState.keywords == keywords) {
            return
        }
        if (searchState is SearchPageState.Searching && searchState.keywords == keywords) {
            return
        }

        this.searchPageState.value = SearchPageState.Searching(keywords)

        requestNotNull(
            action = {
                searchRepository.commonSearch(keywords)
            },
            onSuccess = {
                syncAddResult(keywords, it)
            },
            onFailed = {
                this@SearchUseCase.searchPageState.value =
                    SearchPageState.SearchPageFailed(
                        keywords,
                        R.string.something_wrong_when_search
                    )
            }
        )
    }

    private suspend fun syncAddResult(keywords: String, combineSearchRes: CombineSearchRes) {
        if (combineSearchRes.isEmpty) {
            val searchSuccess =
                SearchPageState.SearchPageSuccess(
                    keywords,
                    R.string.no_content,
                    emptyList(),
                )
            searchPageState.value = searchSuccess
            return
        }

        val searchResults = mutableListOf<SearchResult>()
        val searchSuccess = SearchPageState.SearchPageSuccess(keywords, null, searchResults)
        if (!combineSearchRes.applications.isNullOrEmpty()) {
            searchResults.add(SearchResult.SearchedApplications(combineSearchRes.applications))
        }

        if (!combineSearchRes.users.isNullOrEmpty()) {
            searchResults.add(SearchResult.SearchedUsers(combineSearchRes.users))
        }
        if (!combineSearchRes.groups.isNullOrEmpty()) {
            searchResults.add(SearchResult.SearchedGroups(combineSearchRes.groups))

        }
        if (!combineSearchRes.screens.isNullOrEmpty()) {
            searchResults.add(SearchResult.SearchedScreens(combineSearchRes.screens))
        }
        if (!combineSearchRes.goods.isNullOrEmpty()) {
            searchResults.add(SearchResult.SearchedGoods(combineSearchRes.goods))
        }
        searchPageState.value = searchSuccess
    }

    override fun onComposeDispose(by: String?) {

    }
}