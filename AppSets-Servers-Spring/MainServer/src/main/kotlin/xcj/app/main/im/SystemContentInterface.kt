package xcj.app.main.im

sealed interface SystemContentInterface {

    companion object{
        const val ADD_FRIEND_REQUEST = "add_friend_request"
        const val ADD_FRIEND_REQUEST_FEEDBACK = "add_friend_request_feedback"
        const val JOIN_GROUP_REQUEST = "join_group_request"
        const val JOIN_GROUP_REQUEST_FEEDBACK = "join_group_request_feedback"
    }

    data class FriendRequestJson(
        val requestId: String, val uid: String, val name: String?,
        val avatarUrl: String?, val hello: String
    ) : SystemContentInterface


    data class GroupRequestJson(
        val requestId: String, val uid: String, val name: String?, val avatarUrl: String?,
        val hello: String, val groupId: String, val groupName: String?,
        val groupIconUrl: String?
    ) : SystemContentInterface

    data class FriendRequestFeedbackJson(
        val requestId: String,
        val isAccept: Boolean
    ) : SystemContentInterface


    data class GroupJoinRequestFeedbackJson(
        val requestId: String,
        val isAccept: Boolean
    ) : SystemContentInterface

}