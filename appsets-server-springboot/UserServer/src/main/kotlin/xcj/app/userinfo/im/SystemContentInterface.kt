package xcj.app.userinfo.im

sealed interface SystemContentInterface{
    data class FriendRequestJson(val requestId:String, val uid:String, val name:String?,
                                 val avatarUrl:String?, val hello:String):SystemContentInterface


    data class GroupRequestJson(val requestId:String, val uid:String, val name:String?, val avatarUrl:String?,
                                val hello:String, val groupId:String, val groupName:String?,
                                val groupIconUrl:String?):SystemContentInterface

    data class FriendRequestFeedbackJson(val requestId:String, val isAccept:Boolean):SystemContentInterface


    data class GroupJoinRequestFeedbackJson(val requestId:String, val isAccept:Boolean):SystemContentInterface


}