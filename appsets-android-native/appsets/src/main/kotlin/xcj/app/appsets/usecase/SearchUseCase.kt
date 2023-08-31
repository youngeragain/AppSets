package xcj.app.appsets.usecase

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.db.room.repository.UserGroupsRoomRepository
import xcj.app.appsets.db.room.repository.UserInfoRoomRepository
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.server.api.SearchApi
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.ScreenRepository
import xcj.app.appsets.server.repository.SearchRepository
import xcj.app.appsets.usecase.models.Application
import xcj.app.core.foundation.usecase.NoConfigUseCase


sealed class SearchState {
    data class SearchedUser(val userInfo: UserInfo) : SearchState()
    data class SearchedGroup(val groupInfo: GroupInfo) : SearchState()
    data class SearchedScreen(val screenInfo: UserScreenInfo) : SearchState()
    data class SearchedApplication(val application: Application) : SearchState()
    data class Searching(val tips: String) : SearchState()
    data class SearchingFailed(val tips: String) : SearchState()
    data class SplitTitle(val title: String) : SearchState()
}

class SearchFlow(private val coroutineScope: CoroutineScope) : Flow<Pair<String, Long>> {
    private lateinit var collector: FlowCollector<Pair<String, Long>>
    override suspend fun collect(collector: FlowCollector<Pair<String, Long>>) {
        this.collector = collector
    }

    fun emit(string: String) {
        coroutineScope.launch {
            val currentTimeMillis = System.currentTimeMillis()
            delay(100)
            collector.emit(string to currentTimeMillis)
        }
    }
}

class SearchUseCase(private val coroutineScope: CoroutineScope) : NoConfigUseCase() {
    private val TAG = "SearchUseCase"

    //保存上一次搜索内容
    @Volatile
    var searchStringState: String? = null
    val searchResultListState: MutableList<SearchState> = mutableStateListOf()

    private var searchRepository: SearchRepository? = null

    private var searchFlow: SearchFlow = SearchFlow(coroutineScope)

    fun updateCurrentSearchStr(searchStr: String?) {
        searchStringState = searchStr
        if (searchStr.isNullOrEmpty()) {
            coroutineScope.launch {
                delay(100)
                searchResultListState.clear()
            }
        } else {
            searchFlow.emit(searchStr)
        }
    }

    fun attachToSearchFlow() {
        coroutineScope.launch {
            searchFlow.filter {
                (System.currentTimeMillis() - it.second) > 100
            }.collectLatest {
                search(it.first)
            }
        }
    }

    private suspend fun search(keywords: String) {
        if (keywords.isEmpty())
            return
        if (!LocalAccountManager.isLogged()) {
            coroutineScope.launch(Dispatchers.IO) {
                searchResultListState.clear()
                delay(200)
                searchResultListState.add(SearchState.SearchingFailed("请登录后搜索"))
            }
            return
        }
        coroutineScope.requestNotNull({
            searchResultListState.clear()
            searchResultListState.add(SearchState.Searching("搜索中"))
            checkSearchRepository()
            delay(100)
            searchRepository!!.commonSearch(keywords)
        }, onSuccess = {
            if (searchStringState.isNullOrEmpty()) {
                if (searchResultListState.isNotEmpty())
                    searchResultListState.clear()
                return@requestNotNull
            }
            delay(350)
            if (searchResultListState.isNotEmpty())
                searchResultListState.clear()
            if (searchStringState.isNullOrEmpty()) {
                return@requestNotNull
            }
            if (!it.applications.isNullOrEmpty()) {
                searchResultListState.add(SearchState.SplitTitle("Application"))
                AppSetsRepository.mapIconUrl(it.applications)
                it.applications.forEach { application ->
                    searchResultListState.add(SearchState.SearchedApplication(application))
                }
            }

            if (!it.users.isNullOrEmpty()) {
                searchResultListState.add(SearchState.SplitTitle("用户"))
                UserInfoRoomRepository.mapAvatarUrl(it.users)
                it.users.forEach { userInfo ->
                    searchResultListState.add(SearchState.SearchedUser(userInfo))
                }
            }
            if (!it.groups.isNullOrEmpty()) {
                searchResultListState.add(SearchState.SplitTitle("群组"))
                UserGroupsRoomRepository.mapIconUrl(it.groups)
                it.groups.forEach { groupInfo ->
                    groupInfo.userInfoList?.let { userInfoList ->
                        UserInfoRoomRepository.mapAvatarUrl(
                            userInfoList
                        )
                    }
                    searchResultListState.add(SearchState.SearchedGroup(groupInfo))
                }
            }
            if (!it.screens.isNullOrEmpty()) {
                searchResultListState.add(SearchState.SplitTitle("Screen"))
                ScreenRepository.mapScreensIfNeeded(it.screens)
                it.screens.forEach { screenInfo ->
                    searchResultListState.add(SearchState.SearchedScreen(screenInfo))
                }
            }
        }, onFailed = {
            delay(200)
            searchResultListState.clear()
            val tips = if (!LocalAccountManager.isLogged()) {
                "请登录后搜索"
            } else {
                "搜索时发生错误!"
            }
            searchResultListState.add(SearchState.SearchingFailed(tips))
        })
    }

    private fun checkSearchRepository() {
        if (searchRepository == null) {
            searchRepository = SearchRepository(URLApi.provide(SearchApi::class.java))
        }
    }

    fun onDestroy() {

    }
}