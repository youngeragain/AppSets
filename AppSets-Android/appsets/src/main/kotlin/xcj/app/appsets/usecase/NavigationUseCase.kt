package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.model.TabAction
import xcj.app.appsets.ui.model.TabItem
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.util.PurpleLogger

class NavigationUseCase : ComposeLifecycleAware {

    companion object {
        private const val TAG = "NavigationUseCase"
    }

    var lastRoute: String? = null
    var currentRouteState: MutableState<String?> = mutableStateOf(null)

    val barVisible: MutableState<Boolean> = mutableStateOf(true)
    val barTabItems: MutableState<List<TabItem>> = mutableStateOf(emptyList())

    fun initTabItems() {
        val tabs = mutableListOf<TabItem>()
        val homeFeatures = AppSetsModuleSettings.get().appFeatures.homePageFeatures
        if (homeFeatures.isEnableAppCentral) {
            val appCentralTab = TabItem.SampleTabItem(
                routeName = PageRouteNames.AppsCenterPage,
                icon = xcj.app.compose_share.R.drawable.ic_outline_shopping_bag_24,
                isSelect = true,
                actions = mutableListOf(
                    TabAction.SampleTabAction(
                        route = PageRouteNames.ToolsStartPage,
                        icon = xcj.app.compose_share.R.drawable.ic_architecture_24,
                        action = TabAction.ACTION_APP_TOOLS,
                        description = "app tools"
                    ),
                    TabAction.SampleTabAction(
                        route = PageRouteNames.CreateAppPage,
                        icon = xcj.app.compose_share.R.drawable.ic_appsets_plus,
                        action = TabAction.ACTION_ADD,
                        description = "Create Application"
                    )
                )
            )
            tabs.add(appCentralTab)
        }
        if (homeFeatures.isEnableOutSide) {
            val outSideTab = TabItem.SampleTabItem(
                routeName = PageRouteNames.OutSidePage,
                icon = xcj.app.compose_share.R.drawable.ic_explore_24,
                actions = mutableListOf(
                    TabAction.SampleTabAction(
                        route = PageRouteNames.MediaFallPage,
                        icon = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24,
                        description = "media fall"
                    ),
                    TabAction.SampleTabAction(
                        route = null,
                        action = TabAction.ACTION_REFRESH,
                        icon = xcj.app.compose_share.R.drawable.ic_round_refresh_24,
                        description = "refresh content"
                    ),
                    TabAction.SampleTabAction(
                        route = PageRouteNames.CreateScreenPage,
                        action = TabAction.ACTION_ADD,
                        icon = xcj.app.compose_share.R.drawable.ic_appsets_plus,
                        description = "add screen"
                    )
                )
            )
            tabs.add(outSideTab)
        }

        if (homeFeatures.isEnableConversation) {
            val conversationTab = TabItem.SampleTabItem(
                routeName = PageRouteNames.ConversationOverviewPage,
                icon = xcj.app.compose_share.R.drawable.ic_bubble_chart_24,
                actions = mutableListOf(
                    TabAction.SampleTabAction(
                        route = null,
                        action = TabAction.ACTION_ADD,
                        icon = xcj.app.compose_share.R.drawable.ic_appsets_plus,
                        description = "add actions"
                    )
                )
            )
            tabs.add(conversationTab)
        }

        barTabItems.value = tabs
    }

    fun invalidateTabItemsOnRouteChanged(
        currentRoute: String?,
        by: String?
    ) {
        PurpleLogger.current.d(
            TAG,
            "invalidateTabItemsOnRouteChanged, lastRoute:${this.lastRoute}," +
                    " currentRoute:${currentRoute}, by:$by"
        )
        this.lastRoute = this.currentRouteState.value
        this.currentRouteState.value = currentRoute

        val newTabs = barTabItems.value.map {
            when (it) {
                is TabItem.SampleTabItem -> {
                    it.copy(isSelect = it.routeName == currentRoute)
                }

                is TabItem.PlaybackTabItem -> {
                    it.copy()
                }
            }
        }
        barTabItems.value = newTabs
    }

    fun invalidateTabItemAction(newTabAction: TabAction) {
        PurpleLogger.current.d(TAG, "invalidateTabItemAction")
        val newTabs = barTabItems.value.map { tabItem ->
            if (tabItem.actions.isNullOrEmpty()) {
                tabItem
            } else {
                tabItem.actions?.replaceAll { tabAction ->
                    if (tabAction.actionId == newTabAction.actionId) {
                        newTabAction
                    } else {
                        tabAction
                    }
                }
                when (tabItem) {
                    is TabItem.SampleTabItem -> {
                        tabItem.copy(actions = tabItem.actions)
                    }

                    is TabItem.PlaybackTabItem -> {
                        tabItem.copy(actions = tabItem.actions)
                    }
                }
            }
        }
        barTabItems.value = newTabs
    }

    fun invalidatePlaybackTabItem() {
        PurpleLogger.current.d(TAG, "invalidatePlaybackTabItem")
        val newTabs = barTabItems.value.map {
            when (it) {
                is TabItem.PlaybackTabItem -> {
                    it.copy()
                }

                is TabItem.SampleTabItem -> {
                    it.copy()
                }
            }
        }
        barTabItems.value = newTabs
    }

    override fun onComposeDispose(by: String?) {

    }

    fun getStartPageRoute(): String {
        if (barTabItems.value.isEmpty()) {
            return PageRouteNames.DefaultPage
        }
        return barTabItems.value.first().routeName
    }
}