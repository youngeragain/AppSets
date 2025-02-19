package xcj.app.main.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.common.PagedContent
import xcj.app.main.model.redis.MediaFallDataObject
import xcj.app.main.service.MediaFallService

@RequestMapping("/appsets/mediafall")
@RestController
class MediaFallController(
    private val mediaFallService: MediaFallService
) {
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("/content")
    fun getMediaFallContentsPaged(
        @RequestParam(name = "page") page: Int,
        @RequestParam(name = "pageSize") pageSize: Int
    ): DesignResponse<PagedContent<List<MediaFallDataObject>>> {
        return mediaFallService.getMediaFallContentsPaged(page, pageSize)
    }
}