package xcj.app.appsets.ui.model.state

import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GoodsInfo
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.UserInfo

sealed interface SearchResult {

    val count: Int

    data class SearchedUsers(
        val users: List<UserInfo>,
    ) : SearchResult {
        override val count: Int
            get() = users.size
    }

    data class SearchedGroups(
        val groups: List<GroupInfo>,
    ) : SearchResult {
        override val count: Int
            get() = groups.size
    }

    data class SearchedScreens(
        val screens: List<ScreenInfo>,
    ) : SearchResult {
        override val count: Int
            get() = screens.size
    }

    data class SearchedApplications(
        val applications: List<Application>,
    ) : SearchResult {
        override val count: Int
            get() = applications.size
    }

    data class SearchedGoods(
        val goodsList: List<GoodsInfo>,
    ) : SearchResult {
        override val count: Int
            get() = goodsList.size
    }
}