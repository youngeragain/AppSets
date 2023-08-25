package xcj.app.appsets.im

sealed class CommonURLJson(val fileUrl: String, val fileName: String) {
    interface MediaJson {
        var url: String
        val name: String
    }

    data class MusicURLJson(override var url: String, override val name: String) :
        CommonURLJson(url, name),
        MediaJson

    data class VoiceURLJson(override var url: String, override val name: String) :
        CommonURLJson(url, name),
        MediaJson

    data class VideoURLJson(override var url: String, override val name: String) :
        CommonURLJson(url, name),
        MediaJson

    data class FileURLJson(var url: String, val name: String) : CommonURLJson(url, name)

}

/**
 * 业务数据为type指定
 * 当前有请求添加朋友，请求加入群组
 * @param type friendRequest, groupRequest.....
 *
 */
data class SystemContentJson(val type: String, val content: String) {
    var contentObject: SystemContentInterface? = null
}

sealed interface SystemContentInterface {
    data class FriendRequestJson(
        val requestId: String, val uid: String, val name: String?,
        val avatarUrl: String?, val hello: String
    ) : SystemContentInterface

    data class GroupRequestJson(
        val requestId: String, val uid: String, val name: String?, val avatarUrl: String?,
        val hello: String, val groupId: String, val groupName: String,
        val groupIconUrl: String?
    ) : SystemContentInterface

    interface RequestFeedbackJson : SystemContentInterface {
        val requestId: String
        val isAccept: Boolean
    }

    data class FriendRequestFeedbackJson(
        override val requestId: String,
        override val isAccept: Boolean
    ) : RequestFeedbackJson


    data class GroupJoinRequestFeedbackJson(
        override val requestId: String,
        override val isAccept: Boolean
    ) : RequestFeedbackJson
}

