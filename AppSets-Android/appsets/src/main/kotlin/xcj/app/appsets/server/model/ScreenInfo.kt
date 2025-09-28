package xcj.app.appsets.server.model

import xcj.app.appsets.im.Bio
import java.util.UUID

data class ScreenInfo(
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
) : Bio {

    override val bioId: String
        get() = "SCREEN_${screenId ?: UUID.randomUUID()}"

    override val bioName: String?
        get() = "${userInfo?.bioName}-${bioId}"

    override val bioUrl: Any?
        get() = userInfo?.bioUrl

    fun isAllContentEmpty(): Boolean {
        return screenContent.isNullOrEmpty() && associateUsers.isNullOrEmpty() &&
                associateTopics.isNullOrEmpty() && mediaFileUrls.isNullOrEmpty()
    }
}