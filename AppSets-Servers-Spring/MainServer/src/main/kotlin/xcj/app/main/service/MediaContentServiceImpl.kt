package xcj.app.main.service

import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.main.dao.mongo.MediaContentDao
import xcj.app.main.dao.mysql.UserScreenDao
import xcj.app.main.model.res.MediaContentRes
import kotlin.random.Random

@Service
class MediaContentServiceImpl(
    private val userDao: xcj.app.main.dao.mysql.UserDao,
    private val userScreenDao: UserScreenDao,
    private val mediaContentDao: MediaContentDao
) : MediaContentService {
    companion object {
        private const val MEDIA_CONTENT_TYPE_MUSIC = "music"
        private const val MEDIA_CONTENT_TYPE_VIDEO = "video"
    }

    override fun getMediaContent(contentType: String, page: Int, pageSize: Int): DesignResponse<List<MediaContentRes>> {
        return when (contentType) {
            MEDIA_CONTENT_TYPE_MUSIC -> {
                getMusicMediaContent(page, pageSize)
            }

            MEDIA_CONTENT_TYPE_VIDEO -> {
                getVideoMediaContent(page, pageSize)
            }

            else -> {
                getOthersMediaContent(page, pageSize)
            }
        }
    }

    private fun getOthersMediaContent(page: Int, pageSize: Int): DesignResponse<List<MediaContentRes>> {
        val mediaContentsPaged = mediaContentDao.getMediaContentsPaged(page, pageSize, -1)
        val medias = mediaContentsPaged.map {
            val relateUser = it.relateUserUid?.let { it1 -> userDao.getUserInfoResByUid(it1) }
            val relateUserScreen = it.relateUserScreenId?.let { it1 -> userScreenDao.getScreenResByScreenId(it1) }
            MediaContentRes(
                it.uri,
                it.companionUri,
                relateUser,
                relateUserScreen,
                it.extraInfo,
                null,
                views = Random.nextInt(10000000)
            )
        }
        return DesignResponse(data = medias)
    }

    private fun getVideoMediaContent(page: Int, pageSize: Int): DesignResponse<List<MediaContentRes>> {
        val mediaContentsPaged = mediaContentDao.getMediaContentsPaged(page, pageSize, 1)
        val medias = mediaContentsPaged.map {
            val relateUser = it.relateUserUid?.let { it1 -> userDao.getUserInfoResByUid(it1) }
            val relateUserScreen = it.relateUserScreenId?.let { it1 -> userScreenDao.getScreenResByScreenId(it1) }
            MediaContentRes(
                it.uri,
                it.companionUri,
                relateUser,
                relateUserScreen,
                it.extraInfo,
                null,
                views = Random.nextInt(10000000)
            )
        }
        return DesignResponse(data = medias)
    }

    private fun getMusicMediaContent(page: Int, pageSize: Int): DesignResponse<List<MediaContentRes>> {
        val mediaContentsPaged = mediaContentDao.getMediaContentsPaged(page, pageSize, 0)
        val medias = mediaContentsPaged.map {
            val relateUser = it.relateUserUid?.let { it1 -> userDao.getUserInfoResByUid(it1) }
            val relateUserScreen = it.relateUserScreenId?.let { it1 -> userScreenDao.getScreenResByScreenId(it1) }
            MediaContentRes(
                it.uri,
                it.companionUri,
                relateUser,
                relateUserScreen,
                it.extraInfo,
                null,
                views = Random.nextInt(10000000)
            )
        }
        return DesignResponse(data = medias)
    }
}