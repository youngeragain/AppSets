package xcj.app.main.model.req

data class AddRoleParams(
    val uid: String,
    val role: String,
    val addReason: String?
)