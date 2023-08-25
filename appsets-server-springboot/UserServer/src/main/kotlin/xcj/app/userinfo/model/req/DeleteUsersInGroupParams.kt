package xcj.app.userinfo.model.req

data class DeleteUsersInGroupParams(
    val groupId:String,
    val userIds:List<String>,
    val deleteReason:String?
    )