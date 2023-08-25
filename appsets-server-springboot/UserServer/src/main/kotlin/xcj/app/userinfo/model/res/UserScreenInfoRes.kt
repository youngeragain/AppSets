package xcj.app.userinfo.model.res

import com.fasterxml.jackson.annotation.JsonFormat
import xcj.app.userinfo.model.table.mysql.ScreenMediaFileUrl
import java.io.Serializable
import java.util.Date

data class UserScreenInfoRes(
    val screenId:String?,
    val screenContent:String?,
    val uid:String?,
    val userInfo:UserInfoRes?,
    val likeTimes:Int?,
    val dislikeTimes:Int?,
    val associateTopics:String?,
    val associateUsers:String?,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    val postTime:Date?,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    val editTime:Date?,
    val editTimes:Int?,
    val isPublic:Int?,
    val systemReviewResult:Int?,
    val mediaFileUrls:List<ScreenMediaFileUrl>?
):Serializable{
    constructor():this(null, null, null, null,
        null, null, null, null, null, null,
        null, null, null, null)
}

