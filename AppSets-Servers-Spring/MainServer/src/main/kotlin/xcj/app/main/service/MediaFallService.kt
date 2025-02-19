package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.common.PagedContent
import xcj.app.main.model.redis.MediaFallDataObject

interface MediaFallService {
    fun getMediaFallContentsPaged(
        page: Int, pageSize: Int
    ): DesignResponse<PagedContent<List<MediaFallDataObject>>>
}
