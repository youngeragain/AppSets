package xcj.app.main.dao.mongo

import xcj.app.main.model.table.mongo.MediaContent


interface MediaContentDao {

    fun getMediaContentsPaged(page: Int, pageSize: Int, contentType: Int): List<MediaContent>

    fun addMediaContent(mediaContent: MediaContent): Boolean

}