package xcj.app.userinfo.model.req

data class DeleteFriendsParams(
    val friendUids:List<String>,
    val deleteReason:String?)