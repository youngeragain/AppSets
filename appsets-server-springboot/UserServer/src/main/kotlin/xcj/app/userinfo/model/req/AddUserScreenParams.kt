package xcj.app.userinfo.model.req

import xcj.app.userinfo.model.table.mysql.ScreenMediaFileUrl

data class AddUserScreenParams(
    val screenContent:String?,
    var associateTopics:String?,
    var associateUsers:String?,
    val mediaFileUrls:List<ScreenMediaFileUrl>?,
    val isPublic:Boolean?,
    val addReason:String?
)