package xcj.app.main.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.main.model.common.PagedContent
import xcj.app.main.model.redis.MediaFallDataObject
import xcj.app.main.util.MediaFallHelper

@Service
class MediaFallServiceImpl(
    stringRedisTemplate: StringRedisTemplate
) : MediaFallService {
    private val listOps = stringRedisTemplate.opsForList()
    override fun getMediaFallContentsPaged(
        page: Int,
        pageSize: Int
    ): DesignResponse<PagedContent<List<MediaFallDataObject>>> {
        val pagedContent = PagedContent<List<MediaFallDataObject>>(page, pageSize)
        MediaFallHelper.getMediaFallContentsPaged(listOps, pagedContent)
        return DesignResponse(data = pagedContent)
    }
}