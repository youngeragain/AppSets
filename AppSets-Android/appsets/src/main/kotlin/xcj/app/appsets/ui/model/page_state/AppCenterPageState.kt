package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.ui.model.TipsProvider

sealed interface AppCenterPageState : TipsProvider {
    val apps: List<AppsWithCategory>

    data class Loading(
        override val apps: MutableList<AppsWithCategory>,
        override val tipsIntRes: Int? = null
    ) : AppCenterPageState

    data class LoadSuccess(
        override val apps: List<AppsWithCategory>,
        override val tipsIntRes: Int? = null
    ) : AppCenterPageState
}