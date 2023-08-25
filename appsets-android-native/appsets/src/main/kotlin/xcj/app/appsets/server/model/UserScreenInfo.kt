package xcj.app.appsets.server.model

sealed class ScreenState {
    data class Screen(val userScreenInfo: UserScreenInfo) : ScreenState()
    object NoMore : ScreenState()
}

data class UserScreenInfo(
    val associateTopics: String?,
    val associateUsers: String?,
    val dislikeTimes: Int?,
    val editTime: String?,
    val editTimes: Int?,
    val likeTimes: Int?,
    var mediaFileUrls: List<ScreenMediaFileUrl>?,
    var postTime: String?,
    val screenContent: String?,
    val screenId: String?,
    var isPublic: Int?,
    val systemReviewResult: Int?,
    val uid: String?,
    val userInfo: UserInfo?
) {
    var pictureMediaFileUrls: List<ScreenMediaFileUrl>? = null
    var videoMediaFileUrls: List<ScreenMediaFileUrl>? = null

    fun isAllContentEmpty(): Boolean {
        return screenContent.isNullOrEmpty() && associateUsers.isNullOrEmpty() &&
                associateTopics.isNullOrEmpty() && pictureMediaFileUrls.isNullOrEmpty() &&
                videoMediaFileUrls.isNullOrEmpty()
    }

    var isPublicStr: String = ""
}