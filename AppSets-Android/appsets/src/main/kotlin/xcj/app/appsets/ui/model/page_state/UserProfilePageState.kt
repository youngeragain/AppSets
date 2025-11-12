package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.model.TipsProvider

sealed interface UserProfilePageState : TipsProvider {

    data class LoadSuccess(
        val userInfo: UserInfo,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : UserProfilePageState

    data object Loading :
        UserProfilePageState {
        override val tips: Int = xcj.app.appsets.R.string.loading
        override val subTips: Int? = null
    }

    data object NotFound :
        UserProfilePageState {
        override val tips: Int = xcj.app.appsets.R.string.not_found
        override val subTips: Int? = null
    }

    data class LoadFailed(
        override val tips: Int = xcj.app.appsets.R.string.something_wrong,
        override val subTips: Int? = null
    ) : UserProfilePageState

}