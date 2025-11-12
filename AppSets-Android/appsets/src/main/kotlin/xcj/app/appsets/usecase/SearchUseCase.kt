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
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.CombineSearchRes
import xcj.app.appsets.server.repository.SearchRepository
import xcj.app.appsets.ui.model.page_state.SearchPageState
import xcj.app.appsets.ui.model.state.SearchResult
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.server.HttpRequestFail
import xcj.app.starter.server.request
import kotlin.time.Duration.Companion.milliseconds

class SearchUseCase(
    private val searchRepository: SearchRepository,
) : ComposeLifecycleAware {

    companion object {
        private const val TAG = "SearchUseCase"
    }

    val searchPageState: MutableState<SearchPageState> = mutableStateOf(SearchPageState.Default())

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
        val searchState = searchPageState.value
        if (!LocalAccountManager.isLogged()) {
            if (searchState is SearchPageState.SearchFailed) {
                return
            }
            searchPageState.value =
                SearchPageState.SearchFailed(
                    keywords = keywords,
                    tips = xcj.app.appsets.R.string.login_required,
                    subTips = xcj.app.appsets.R.string.login_to_search
                )
            return
        }
        if (searchState is SearchPageState.SearchSuccess &&
            searchState.keywords == keywords
        ) {
            return
        }
        if (keywords.isEmpty()) {
            searchPageState.value =
                SearchPageState.SearchSuccess(
                    keywords = keywords,
                    tips = xcj.app.appsets.R.string.no_content,
                    subTips = null,
                    results = emptyList(),
                )
            return
        }


        this.searchPageState.value =
            SearchPageState.Searching(keywords = keywords)

        request {
            searchRepository.commonSearch(keywords)
        }.onSuccess {
            handleSearchResult(keywords, it)
        }.onFailure { exception ->
            if (exception is HttpRequestFail) {
                when (exception.response?.code) {
                    -3 -> {
                        this@SearchUseCase.searchPageState.value =
                            SearchPageState.SearchFailed(
                                keywords = keywords,
                                tips = xcj.app.appsets.R.string.login_required,
                                subTips = xcj.app.appsets.R.string.expired_information
                            )
                    }

                    else -> {
                        this@SearchUseCase.searchPageState.value =
                            SearchPageState.SearchFailed(
                                keywords = keywords,
                                tips = xcj.app.appsets.R.string.something_wrong
                            )
                    }
                }
            }

        }
    }

    private suspend fun handleSearchResult(
        keywords: String,
        combineSearchRes: CombineSearchRes
    ) {
        if (combineSearchRes.isEmpty) {
            val searchSuccess =
                SearchPageState.SearchSuccess(
                    keywords = keywords,
                    tips = xcj.app.appsets.R.string.no_content,
                    subTips = null,
                    results = emptyList(),
                )
            searchPageState.value = searchSuccess
            return
        }

        val searchResults = mutableListOf<SearchResult>()
        val searchSuccess = SearchPageState.SearchSuccess(
            keywords = keywords,
            tips = null,
            subTips = null,
            results = searchResults
        )
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