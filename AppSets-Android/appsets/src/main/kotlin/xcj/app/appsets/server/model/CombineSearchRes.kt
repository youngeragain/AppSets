package xcj.app.appsets.server.model

data class CombineSearchRes(
    val applications: List<Application>? = null,
    val screens: List<ScreenInfo>? = null,
    val users: List<UserInfo>? = null,
    val groups: List<GroupInfo>? = null
) {
    val isEmpty: Boolean
        get() {
            return applications.isNullOrEmpty() && screens.isNullOrEmpty() && users.isNullOrEmpty() && groups.isNullOrEmpty()
        }
}