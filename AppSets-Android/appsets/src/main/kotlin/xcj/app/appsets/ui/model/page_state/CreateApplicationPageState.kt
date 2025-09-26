package xcj.app.appsets.ui.model.page_state

import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.model.TipsProvider

sealed interface CreateApplicationPageState : TipsProvider {
    val applicationForCreate: ApplicationForCreate

    data class NewApplicationPage(
        override val applicationForCreate: ApplicationForCreate = ApplicationForCreate(),
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateApplicationPageState

    data class Creating(
        override val applicationForCreate: ApplicationForCreate,
        override val tips: Int = xcj.app.appsets.R.string.creating,
        override val subTips: Int? = null
    ) : CreateApplicationPageState

    data class CreateSuccessPage(
        override val applicationForCreate: ApplicationForCreate,
        override val tips: Int = xcj.app.appsets.R.string.create_application_success,
        override val subTips: Int? = null
    ) : CreateApplicationPageState

    data class CreateFailedPage(
        override val applicationForCreate: ApplicationForCreate,
        override val tips: Int? = null,
        override val subTips: Int? = null
    ) : CreateApplicationPageState

}