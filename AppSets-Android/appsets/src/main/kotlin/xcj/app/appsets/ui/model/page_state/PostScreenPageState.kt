package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.TipsProvider

sealed interface PostScreenPageState : TipsProvider {

    val screenInfoForCreate: ScreenInfoForCreate

    data class NewPostScreenPage(
        override val screenInfoForCreate: ScreenInfoForCreate = ScreenInfoForCreate(),
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : PostScreenPageState

    data class Posting(
        override val screenInfoForCreate: ScreenInfoForCreate,
        override val tips: Int = xcj.app.appsets.R.string.adding,
        override val subTips: Int? = null
    ) : PostScreenPageState

    data class PostSuccessPage(
        override val screenInfoForCreate: ScreenInfoForCreate,
        override val tips: Int = xcj.app.appsets.R.string.create_success,
        override val subTips: Int? = null
    ) : PostScreenPageState

    data class PostFailedPage(
        override val screenInfoForCreate: ScreenInfoForCreate,
        override val tips: Int = xcj.app.appsets.R.string.create_failed,
        override val subTips: Int? = null
    ) : PostScreenPageState
}