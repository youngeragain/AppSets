package xcj.app.main.dao.mongo

import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import xcj.app.main.model.table.mongo.MediaContent
import xcj.app.util.PurpleLogger

@Component
class MediaContentDaoImpl(
    private val mongoTemplate: MongoTemplate
) : MediaContentDao {
    companion object {
        private const val TAG = "MediaContentDaoImpl"
    }

    override fun getMediaContentsPaged(page: Int, pageSize: Int, contentType: Int): List<MediaContent> {
        val tempPage = if (page > 0) {
            page - 1
        } else {
            page
        }
        PurpleLogger.current.d(TAG, "getMediaContentsPaged, contentType:$contentType, page:$page, size:$pageSize")
        val pageAble = PageRequest.of(tempPage, pageSize)
        return mongoTemplate.find(
            Query.query(
                Criteria.where("type").`is`(contentType)
            ).with(pageAble),
            MediaContent::class.java,
            "MediaContent"
        )
    }

    override fun addMediaContent(mediaContent: MediaContent): Boolean {
        kotlin.runCatching {
            mongoTemplate.insert(mediaContent)
        }.onSuccess {
            return true
        }.onFailure {
            return false
        }
        return false
    }
}