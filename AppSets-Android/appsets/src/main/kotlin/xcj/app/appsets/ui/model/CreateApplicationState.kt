package xcj.app.appsets.ui.model


sealed interface CreateApplicationState : TipsState {
    val applicationForCreate: ApplicationForCreate

    data class NewApplication(
        override val applicationForCreate: ApplicationForCreate = ApplicationForCreate(),
        override val tips: Int? = null
    ) : CreateApplicationState

    data class Creating(
        override val applicationForCreate: ApplicationForCreate,
        override val tips: Int = xcj.app.appsets.R.string.creating
    ) : CreateApplicationState

    data class CreateSuccess(
        override val applicationForCreate: ApplicationForCreate,
        override val tips: Int = xcj.app.appsets.R.string.create_application_success
    ) : CreateApplicationState

    data class CreateFailed(
        override val applicationForCreate: ApplicationForCreate,
        override val tips: Int? = null
    ) : CreateApplicationState

}