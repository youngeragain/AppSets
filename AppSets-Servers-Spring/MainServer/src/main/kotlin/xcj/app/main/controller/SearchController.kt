package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.res.CombineSearchRes
import xcj.app.main.service.SearchService


@RestController
@RequestMapping("/search")
class SearchController(
    private val searchService: SearchService
) {

    @ApiDesignPermission.LoginRequired
    @GetMapping("common")
    fun combineSearch(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestParam(name = "keywords") keywords: String,
        @RequestParam(name = "types", required = false) types: String? = null,
        @RequestParam(name = "page", required = false) page: Int? = 1,
        @RequestParam(name = "size", required = false) pageSize: Int? = 20,
    ): DesignResponse<CombineSearchRes> {
        return searchService.searchByKeywords(keywords, types, page, pageSize)
    }
}