package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.R
import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.TipsProvider

sealed interface PostScreenPageState : TipsProvider {

    val screenInfoForCreate: ScreenInfoForCreate

    data class NewPostScreenPage(
        override val screenInfoForCreate: ScreenInfoForCreate = ScreenInfoForCreate(),
        override val tipsIntRes: Int? = null,
        override val subTipsIntRes: Int? = null
    ) : PostScreenPageState

    data class Posting(
        override val screenInfoForCreate: ScreenInfoForCreate,
        override val tipsIntRes: Int = R.string.adding,
        override val subTipsIntRes: Int? = null
    ) : PostScreenPageState

    data class PostSuccessPage(
        override val screenInfoForCreate: ScreenInfoForCreate,
        override val tipsIntRes: Int = R.string.create_success,
        override val subTipsIntRes: Int? = null
    ) : PostScreenPageState

    data class PostFailedPage(
        override val screenInfoForCreate: ScreenInfoForCreate,
        override val tipsIntRes: Int = R.string.create_failed,
        override val subTipsIntRes: Int? = null
    ) : PostScreenPageState
}