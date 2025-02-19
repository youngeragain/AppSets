package xcj.app.main.im

import xcj.app.ApiDesignPermission

data class MessageToInfo(
    val toType: String,
    val id: String,
    val name: String?,
    val iconUrl: String?,
    val roles: String?
) {
    companion object{
        const val TO_TYPE_O2O = "one2one"
        const val TO_TYPE_O2M = "one2many"

        const val ROLE_ADMIN = ApiDesignPermission.UserRoleRequired.ROLE_ADMIN
    }
}