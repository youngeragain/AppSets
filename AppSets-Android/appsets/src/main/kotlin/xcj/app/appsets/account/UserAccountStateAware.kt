package xcj.app.appsets.account

interface UserAccountStateAware {
    fun onUserLogout(by: String?) {}
    fun onUserLogin(by: String?) {}
}