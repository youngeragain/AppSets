package xcj.app.appsets.server.model

data class CombineSearchRes(
    val applications: List<Application>? = null,
    val screens: List<ScreenInfo>? = null,
    val users: List<UserInfo>? = null,
    val groups: List<GroupInfo>? = null,
    val goods: List<Any>? = null,
) {
    val isEmpty: Boolean
        get() {
            if (!applications.isNullOrEmpty()) {
                return false
            }
            if (!screens.isNullOrEmpty()) {
                return false
            }
            if (!users.isNullOrEmpty()) {
                return false
            }
            if (!groups.isNullOrEmpty()) {
                return false
            }
            if (!goods.isNullOrEmpty()) {
                return false
            }
            return true
        }
}