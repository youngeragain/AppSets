package xcj.app.main.util

import org.springframework.data.redis.core.ListOperations
import xcj.app.main.dao.mongo.MediaContentDao
import xcj.app.main.dao.mysql.UserDao
import xcj.app.main.model.common.PagedContent
import xcj.app.main.model.redis.MediaFallDataObject
import xcj.app.main.model.req.AddUserScreenParams
import xcj.app.main.model.table.mongo.MediaContent

object MediaFallHelper {

    @JvmStatic
    fun addMediaContentToFall(
        mediaContentDao: MediaContentDao,
        userDao: UserDao,
        uid: String,
        addUseScreenParams: AddUserScreenParams,
        screenId: String
    ) {
        addUseScreenParams.mediaFileUrls?.firstOrNull {
            !it.mediaFileCompanionUrl.isNullOrEmpty()
        }?.let {
            val mediaContent =
                MediaContent(
                    id = null,
                    type = 1,
                    uri = it.mediaFileUrl ?: "",
                    companionUri = it.mediaFileCompanionUrl,
                    relateUserUid = uid,
                    relateUserScreenId = screenId,
                    extraInfo = it.mediaDescription?.let { "?name=$it" }
                )
            mediaContentDao.addMediaContent(mediaContent)
        }
    }

    @JvmStatic
    fun getMediaFallContentsPaged(
        listRedisTemplate: ListOperations<String, String>,
        pagedContent: PagedContent<List<MediaFallDataObject>>
    ) {
        if (pagedContent.page < 0)
            return
        if (pagedContent.pageSize < 0)
            return
    }
}

