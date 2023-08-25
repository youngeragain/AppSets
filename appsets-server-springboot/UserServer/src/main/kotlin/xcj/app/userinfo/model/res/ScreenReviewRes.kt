package xcj.app.userinfo.model.res

import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.util.*

class ScreenReviewRes(
    val reviewId:String?,
    val content:String?,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    val reviewTime: Date?,
    val likes:Int?,
    val dislikes:Int?,
    val pid:String?,
    val uid:String?,
    val screenId:String?,
    val reviewPassed:Int?,
    val withdraw:Int?,
    var isPublic:Int?,
    var userInfo:UserInfoRes?
):Serializable{
    constructor():this(null, null, null,
        null, null, null, null, null, null, null, null, null )
}