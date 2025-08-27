package xcj.app.appsets.ui.model.state

import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.UserInfo

sealed interface SearchResult {
    data class SearchedUsers(
        val users: List<UserInfo>,
    ) : SearchResult

    data class SearchedGroups(
        val groups: List<GroupInfo>,
    ) : SearchResult

    data class SearchedScreens(
        val screens: List<ScreenInfo>,
    ) : SearchResult

    data class SearchedApplications(
        val applications: List<Application>,
    ) : SearchResult

    data class SearchedGoods(
        val goodsList: List<Any>,
    ) : SearchResult
}