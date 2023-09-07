package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.TabItemState
import xcj.app.core.foundation.usecase.NoConfigUseCase

@UnstableApi
class BottomMenuUseCase() : NoConfigUseCase() {
    var mSavedLastNavDestination: String? = null

    val tabVisibilityState: MutableState<Boolean> = mutableStateOf(true)
    val tabItemsState: MutableList<TabItemState> = mutableStateListOf()

    fun initTabItems() {
        if (tabItemsState.isNotEmpty())
            return
        with(tabItemsState) {
            val tab0 = TabItemState(
                type = PageRouteNameProvider.StartPage,
                iconRes = R.drawable.outline_play_circle_outline_24,
                name = "开始",
                isSelect = mutableStateOf(true)
            )
            val tab1 = TabItemState(
                type = PageRouteNameProvider.AppSetsCenterPage,
                iconRes = R.drawable.outline_shopping_bag_24,
                name = "应用"
            )
            val tab2 = TabItemState(
                type = PageRouteNameProvider.OutSidePage,
                iconRes = R.drawable.outline_explore_24,
                name = "外面"
            )

            val tab3 = TabItemState(
                type = PageRouteNameProvider.ConversationOverviewPage,
                iconRes = R.drawable.outline_bubble_chart_24,
                name = "对话"
            )
            add(tab0)
            add(tab1)
            add(tab2)
            add(tab3)
        }
    }

    fun invalidateWhenMainCompose(navCurrentRoute: String? = PageRouteNameProvider.StartAllAppsPage) {
        if (!tabVisibilityState.value)
            tabVisibilityState.value = true
        mSavedLastNavDestination = null
        tabItemsState.forEach {
            it.isSelect.value = it.type == navCurrentRoute
        }
    }
}