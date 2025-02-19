package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.res.MediaContentRes

interface MediaContentService {
    fun getMediaContent(contentType: String, page: Int, pageSize: Int): DesignResponse<List<MediaContentRes>>
}