package xcj.app.main.model.req

data class UpdateRoleParams(
    val uid: String,
    val oldRole: String,
    val newRole: String
)