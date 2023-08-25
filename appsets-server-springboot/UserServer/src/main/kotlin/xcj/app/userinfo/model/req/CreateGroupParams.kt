package xcj.app.userinfo.model.req

data class CreateGroupParams(
    val name:String,
    val type:Int?,
    val iconUrl:String?,
    val maxMembers:Int?,
    val isPublic:Boolean?,
    val introduction:String?,
    val userIds:List<String>?
)