package xcj.app.appsets.im.model

sealed interface SystemContentInterface {

    companion object {
        const val ADD_FRIEND_REQUEST = "add_friend_request"
        const val ADD_FRIEND_REQUEST_FEEDBACK = "add_friend_request_feedback"
        const val JOIN_GROUP_REQUEST = "join_group_request"
        const val JOIN_GROUP_REQUEST_FEEDBACK = "join_group_request_feedback"
    }

}