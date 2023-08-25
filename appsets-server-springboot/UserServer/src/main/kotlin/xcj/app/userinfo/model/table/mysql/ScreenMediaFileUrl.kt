package xcj.app.userinfo.model.table.mysql

import java.io.Serializable

data class ScreenMediaFileUrl(
    val mediaFileUrl:String?,
    val mediaFileCompanionUrl:String?,
    val mediaType:String?,
    val mediaDescription:String?,
    var x18Content:Int?
):Serializable