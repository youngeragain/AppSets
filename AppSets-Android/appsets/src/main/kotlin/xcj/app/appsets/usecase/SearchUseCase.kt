@file:OptIn(FlowPreview::class)

package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.CombineSearchRes
import xcj.app.appsets.server.repository.SearchRepository
import xcj.app.appsets.ui.model.SearchResult
import xcj.app.appsets.ui.model.SearchState
import xcj.app.compose_share.dynamic.IComposeDispose
import xcj.app.starter.server.requestNotNull

class SearchUseCase(
    private val coroutineScope: CoroutineScope,
    private val searchRepository: SearchRepository,
) : IComposeDispose {

    companion object {
        private const val TAG = "SearchUseCase"
    }

    //保存上一次搜索内容
    val searchState: MutableState<SearchState> = mutableStateOf<SearchState>(SearchState.None())

    private val searchFlow: MutableStateFlow<String> = MutableStateFlow("").apply {
        debounce(200)
        buffer(5, BufferOverflow.SUSPEND)
    }

    private var searchJob: Job? = null

    fun updateKeywords(keywords: String) {
        searchFlow.value = keywords
    }

    fun attachToSearchFlow() {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            searchFlow.collect {
                search(it.trim())
            }
        }
    }

    private suspend fun search(keywords: String) {
        if (!LocalAccountManager.isLogged()) {
            searchState.value =
                SearchState.SearchFailed(keywords, xcj.app.appsets.R.string.login_to_search)
            return
        }

        if (keywords.isEmpty()) {
            searchState.value =
                SearchState.SearchSuccess(
                    keywords,
                    emptyList(),
                    xcj.app.appsets.R.string.no_content
                )
            return
        }

        val searchState = searchState.value

        if (searchState is SearchState.SearchSuccess && searchState.keywords == keywords) {
            return
        }
        if (searchState is SearchState.Searching && searchState.keywords == keywords) {
            return
        }

        this.searchState.value = SearchState.Searching(keywords)

        requestNotNull(
            action = {
                searchRepository.commonSearch(keywords)
            },
            onSuccess = {
                syncAddResult(keywords, it)
            },
            onFailed = {
                this@SearchUseCase.searchState.value =
                    SearchState.SearchFailed(
                        keywords,
                        xcj.app.appsets.R.string.something_wrong_when_search
                    )
            }
        )
    }

    private suspend fun syncAddResult(keywords: String, combineSearchRes: CombineSearchRes) {
        delay(450)
        if (combineSearchRes.isEmpty) {
            val searchSuccess =
                SearchState.SearchSuccess(
                    keywords,
                    emptyList(),
                    xcj.app.appsets.R.string.no_content
                )
            searchState.value = searchSuccess
            return
        }

        val searchResults = mutableListOf<SearchResult>()
        val searchSuccess = SearchState.SearchSuccess(keywords, searchResults)
        if (!combineSearchRes.applications.isNullOrEmpty()) {
            searchResults.add(SearchResult.SplitTitle(xcj.app.appsets.R.string.application))
            searchResults.add(SearchResult.SearchedApplications(combineSearchRes.applications))
        }

        if (!combineSearchRes.users.isNullOrEmpty()) {
            searchResults.add(SearchResult.SplitTitle(xcj.app.appsets.R.string.user))
            combineSearchRes.users.mapTo(searchResults) {
                SearchResult.SearchedUser(it)
            }
        }
        if (!combineSearchRes.groups.isNullOrEmpty()) {
            searchResults.add(SearchResult.SplitTitle(xcj.app.appsets.R.string.group))
            combineSearchRes.groups.mapTo(searchResults) {
                SearchResult.SearchedGroup(it)
            }

        }
        if (!combineSearchRes.screens.isNullOrEmpty()) {
            searchResults.add(SearchResult.SplitTitle(xcj.app.appsets.R.string.appsets_screen))
            combineSearchRes.screens.mapTo(searchResults) {
                SearchResult.SearchedScreen(it)
            }
        }
        searchState.value = searchSuccess
    }

    fun detachToSearchFlow() {
        searchJob?.cancel()
    }

    override fun onComposeDispose(by: String?) {

    }
}