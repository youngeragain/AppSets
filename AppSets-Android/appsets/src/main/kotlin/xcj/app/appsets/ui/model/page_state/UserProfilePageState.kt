package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.R
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.model.TipsProvider

sealed interface UserProfilePageState : TipsProvider {

    data class LoadSuccess(
        val userInfo: UserInfo,
        override val tipsIntRes: Int? = null,
        override val subTipsIntRes: Int? = null
    ) : UserProfilePageState

    data object Loading :
        UserProfilePageState {
        override val tipsIntRes: Int? = R.string.loading
        override val subTipsIntRes: Int? = null
    }

    data object NotFound :
        UserProfilePageState {
        override val tipsIntRes: Int? = R.string.not_found
        override val subTipsIntRes: Int? = null
    }

}